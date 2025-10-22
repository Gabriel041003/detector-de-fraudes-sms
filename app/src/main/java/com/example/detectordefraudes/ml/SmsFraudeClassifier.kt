package com.example.detectordefraudes.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.text.Normalizer

class SmsFraudeClassifier(private val context: Context) {

    private val modelPath = "modelo_sms.tflite"   // seus nomes no assets
    private val vocabPath = "vocab.json"
    private val SEQ_LEN = 64
    private val THRESHOLD = 0.35f                  // puxe recall p/ golpes
    private val DEBUG = true                       // mude p/ false em produção

    private val interpreter: Interpreter by lazy { Interpreter(loadModelFile()) }
    private val vocab: Map<String, Int> by lazy { loadVocabJson(vocabPath) }
    private val unkId: Int by lazy { vocab["[UNK]"] ?: 1 } // ajuste se seu vocab tiver outra chave

    data class Result(
        val probFraude: Float,   // P(fraude), saída do sigmoid
        val confianca: Float,    // confiança coerente com a classe prevista
        val isFraude: Boolean,
        val debugNorm: String? = null,
        val debugFirstIds: String? = null
    )

    fun classify(rawText: String): Result {
        val norm = normalize(rawText)
        val ids1xSeq = toIds1xSeq(norm)
        val out = Array(1) { FloatArray(1) }
        interpreter.run(ids1xSeq, out)

        // P(fraude) do modelo
        var prob = out[0][0].coerceIn(0f, 1f)

        // Boost heurístico leve p/ golpes sem link (temporário, ajuda no dataset inicial)
        prob = (prob + heuristicBoost(norm)).coerceIn(0f, 1f)

        val isFraude = prob >= THRESHOLD
        val confianca = if (isFraude) prob else 1f - prob

        if (DEBUG) {
            val ids = ids1xSeq[0].take(20).joinToString(",")
            Log.d("IA_SMS", "norm='$norm'")
            Log.d("IA_SMS", "ids[0..20]=[$ids]")
            Log.d("IA_SMS", "probFraude=$prob  isFraude=$isFraude  conf=$confianca")
        }

        return Result(
            probFraude = prob,
            confianca = confianca,
            isFraude = isFraude,
            debugNorm = if (DEBUG) norm else null,
            debugFirstIds = null
        )
    }

    // --- Arquivos ---
    private fun loadModelFile(): MappedByteBuffer {
        val afd = context.assets.openFd(modelPath)
        FileInputStream(afd.fileDescriptor).use { fis ->
            return fis.channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.length)
        }
    }

    private fun loadVocabJson(assetName: String): Map<String, Int> {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        val obj = JSONObject(json)
        val map = HashMap<String, Int>()
        obj.keys().forEach { k -> map[k] = obj.getInt(k) }
        return map
    }

    // --- Pré-processamento (idêntico ao treino) ---
    private fun normalize(s: String): String {
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

    private fun toIds1xSeq(text: String): Array<IntArray> {
        val padId = 0
        val tokens = text.split(" ").filter { it.isNotBlank() }
        val ids = IntArray(SEQ_LEN) { padId }
        val mapped = tokens.take(SEQ_LEN).map { tok -> vocab[tok] ?: unkId }
        for (i in mapped.indices) ids[i] = mapped[i]
        return arrayOf(ids)
    }

    // Pequeno empurrão p/ padrões clássicos de golpe (sem link)
    private fun heuristicBoost(normLower: String): Float {
        val s = normLower
        var boost = 0f
        var hits = 0

        val keys = listOf(
            "parabens", "parabén", "voce ganhou", "você ganhou",
            "clique aqui", "resgatar premio", "resgatar premio",
            "premio", "prêmio",
            "bloqueado", "suspenso", "regularize", "atualize cadastro",
            "ligue para o banco", "cartao foi bloqueado", "seu pix foi bloqueado"
        )
        for (k in keys) if (s.contains(k)) hits++

        if (hits >= 2) boost += 0.15f
        if (s.contains("pix")) boost += 0.10f
        return boost.coerceAtMost(0.25f) // nunca passa de +0.25
    }
}
