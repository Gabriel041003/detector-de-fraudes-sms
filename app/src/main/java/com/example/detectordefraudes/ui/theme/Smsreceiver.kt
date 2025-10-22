package com.example.detectordefraudes.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.detectordefraudes.R
import com.example.detectordefraudes.ml.SmsAnalyzer
import com.example.detectordefraudes.ml.TokenizerLite
import java.text.Normalizer

/**
 * SmsReceiver completo — contém:
 * - normalização idêntica ao treino
 * - chamada ao TokenizerLite e SmsAnalyzer
 * - cálculo correto de confiança (isFraude ? prob : 1 - prob)
 * - heurísticas para aumentar/diminuir score
 * - regra: se mensagem curta e sem sinais de risco -> legítimo
 * - logging para debug
 */
class SmsReceiver : BroadcastReceiver() {

    // ajuste o THRESHOLD conforme seu apetite de risco (menor -> mais recall)
    private val THRESHOLD = 0.35f
    private val DEBUG = true

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle: Bundle = intent.extras ?: return
        val pdus = bundle.get("pdus") as? Array<*> ?: return
        val format = bundle.getString("format")

        // Monta o texto completo (suporta multipart SMS)
        val rawText = buildString {
            for (pdu in pdus) {
                val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(pdu as ByteArray, format)
                } else {
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
                append(msg.messageBody ?: "")
            }
        }

        if (DEBUG) Log.d("SMS_RECEBIDO", "RAW SMS: $rawText")

        try {
            // Normaliza do mesmo jeito que o treino (importantíssimo)
            val norm = normalizeLikeTraining(rawText)

            // Tokenize (verifique que TokenizerLite faz o que esperamos)
            val tokenizer = TokenizerLite(context)
            val ids = tokenizer.textToIds(norm)

            // Previsão do modelo (P(fraude))
            val analyzer = SmsAnalyzer(context)
            var prob = analyzer.predict(ids).coerceIn(0f, 1f)

            // Ajustes heurísticos (aumenta se houver sinais de golpe; diminui com sinais benígnos)
            prob = adjustWithHeuristics(norm, prob).coerceIn(0f, 1f)

            // Regra de segurança:
            // se mensagem curta e sem sinais de risco, força legítimo (reduz falsos positivos)
            val riskHits = riskSignals(norm)
            val isShort = norm.length < 40
            val noRisk = (riskHits == 0)

            val isFraude = if (noRisk && isShort) {
                false
            } else {
                prob >= THRESHOLD
            }

            val confianca = if (isFraude) prob else 1f - prob
            val titulo = if (isFraude) "🔴 SMS suspeito" else "🟢 SMS legítimo"
            val msg = "Confiança: ${(confianca * 100).toInt()}%"

            if (DEBUG) {
                Log.d("SMS_RESULT",
                    "norm='$norm' | probFraude=${"%.3f".format(prob)} | riskHits=$riskHits | short=$isShort | isFraude=$isFraude | conf=${"%.3f".format(confianca)}")
            }

            // Mostra notificação
            notify(context, titulo, msg)

            // AQUI você pode adicionar lógica de persistência para feedback do usuário:
            // salvar rawText, norm, prob, isFraude em um CSV/local DB para re-treino futuro.

        } catch (e: Exception) {
            Log.e("SMS_RECEBIDO", "Erro ao classificar SMS: ${e.message}", e)
        }
    }

    // -----------------------------
    // Notificação simples
    // -----------------------------
    private fun notify(context: Context, title: String, text: String) {
        val chanId = "fraud_result"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                chanId,
                "Resultados Detector de Fraudes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(chan)
        }
        val notif = NotificationCompat.Builder(context, chanId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
    }

    // -----------------------------
    // Normalização (mesma do Python)
    // -----------------------------
    private fun normalizeLikeTraining(s: String): String {
        var t = s.lowercase().trim()
        t = Normalizer.normalize(t, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        t = t.replace(Regex("https?://\\S+|www\\.\\S+"), "<url>")
        t = t.replace(Regex("\\b\\d{11}\\b"), "<cpf>")
        t = t.replace(Regex("\\+?\\d[\\d\\s\\-\\(\\)]{7,}\\d"), "<tel>")
        t = t.replace(Regex("\\b(chave\\s+pix|pix)\\b"), "<pix>")
        t = t.replace("\\s+".toRegex(), " ")
        return t
    }

    // -----------------------------
    // Heurísticas: combinação risco / benigno
    // -----------------------------
    private fun adjustWithHeuristics(norm: String, baseProb: Float): Float {
        var prob = baseProb
        val risk = riskSignals(norm)
        val safe = safeSignals(norm)

        // cada sinal de risco soma +0.10, até +0.30
        prob += (0.10f * risk).coerceAtMost(0.30f)

        // cada sinal benigno subtrai -0.08, até -0.24
        prob -= (0.08f * safe).coerceAtMost(0.24f)

        return prob
    }

    // Conta sinais clássicos de risco (palavras/expressões)
    private fun riskSignals(s: String): Int {
        val keys = listOf(
            "parabens","parabéns","voce ganhou","você ganhou","clique aqui","resgatar premio","resgatar prêmio",
            "premio","prêmio","bloqueado","suspenso","regularize","atualize cadastro","confirmar dados",
            "senha expira","redefina","libere seu cartao","cartao foi bloqueado","seu pix foi bloqueado",
            "pix bloqueado","banco","itau","bradesco","caixa","compra nao reconhecida","nao reconhece",
            "mensagem de voz aguardando","<url>","<pix>"
        )
        var hits = 0
        for (k in keys) if (s.contains(k)) hits++
        return hits
    }

    // Conta sinais de conversas cotidianas (ajuda a reduzir falsos positivos)
    private fun safeSignals(s: String): Int {
        val benign = listOf(
            "vamos jantar","vamos almoçar","vamos sair","cheguei","to chegando","estou chegando",
            "bom dia","boa tarde","boa noite","ok","blz","beleza","tudo bem","td bem",
            "obrigado","valeu","ate mais","até mais","te ligo","me liga","beijo","bjs","kkk","rs",
            "onde voce esta","onde você está","partiu","vamos hoje","hoje a noite","hj a noite","hj",
            "quer sair","quer jantar","quer almoçar","posso ir","to indo","chegando"
        )
        var hits = 0
        for (k in benign) if (s.contains(k)) hits++
        return hits
    }
}
