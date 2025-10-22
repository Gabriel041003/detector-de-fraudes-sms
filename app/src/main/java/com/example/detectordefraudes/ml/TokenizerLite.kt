package com.example.detectordefraudes.ml



import android.content.Context
import com.google.gson.Gson
import java.text.Normalizer
import java.util.Locale

data class VocabConfig(
    val word_index: Map<String, Int>,
    val oov_index: Int,
    val max_len: Int,
    val num_words: Int
)

/**
 * Converte o texto do SMS em um vetor de índices (IntArray) usando o vocab.json
 * O pré-processamento precisa combinar com o que você usou no treino.
 */
class TokenizerLite(context: Context) {
    private val vocab: VocabConfig

    init {
        val json = context.assets.open("vocab.json").bufferedReader().use { it.readText() }
        vocab = Gson().fromJson(json, VocabConfig::class.java)
    }

    private fun normalize(text: String): String {
        var t = text.lowercase(Locale.getDefault())
        // remove acentos
        t = Normalizer.normalize(t, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        // mantém letras, números e espaço
        t = t.replace("[^a-z0-9 ]".toRegex(), " ")
        // colapsa espaços
        t = t.replace("\\s+".toRegex(), " ").trim()
        return t
    }

    /** Converte texto para IDs com padding à esquerda (pre), tamanho max_len. */
    fun textToIds(text: String): IntArray {
        val tokens = normalize(text).split(" ").filter { it.isNotBlank() }
        val idsList = tokens.map { w -> vocab.word_index[w] ?: vocab.oov_index }

        val out = IntArray(vocab.max_len) { 0 }
        val slice = if (idsList.size > vocab.max_len) idsList.takeLast(vocab.max_len) else idsList
        val start = (vocab.max_len - slice.size).coerceAtLeast(0)
        slice.forEachIndexed { i, v -> out[start + i] = v }
        return out
    }

    val maxLen: Int get() = vocab.max_len
}
