package com.example.detectordefraudes.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.detectordefraudes.ml.SmsAnalyzer
import com.example.detectordefraudes.ml.TokenizerLite

data class Denuncia(
    val texto: String,
    val status: String = "Em análise",
    val probabilidade: Float? = null
)

class DenunciaViewModel(app: Application) : AndroidViewModel(app) {
    private val _denuncias = mutableStateListOf<Denuncia>()
    val denuncias: List<Denuncia> get() = _denuncias

    private val tokenizer by lazy { TokenizerLite(getApplication()) }
    private val analyzer  by lazy { SmsAnalyzer(getApplication()) }

    fun analisarDenuncia(texto: String) {
        val ids = tokenizer.textToIds(texto)
        val prob = analyzer.predict(ids)
        val classe = if (prob >= 0.5f) "fraude" else "legitimo"
        _denuncias.add(
            Denuncia(
                texto = texto,
                status = classe,
                probabilidade = prob
            )
        )
    }
}
