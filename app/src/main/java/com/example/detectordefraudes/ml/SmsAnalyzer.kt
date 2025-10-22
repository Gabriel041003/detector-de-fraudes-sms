package com.example.detectordefraudes.ml


import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Carrega o modelo TFLite (modelo_sms.tflite) de assets e executa inferência.
 */
class SmsAnalyzer(context: Context) {

    private val interpreter: Interpreter = Interpreter(loadModelFile(context))

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val afd = context.assets.openFd("modelo_sms.tflite")
        FileInputStream(afd.fileDescriptor).use { fis ->
            val channel = fis.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
        }
    }

    /**
     * Executa o modelo. Entrada deve ser IntArray com length = maxLen (ex.: 20).
     * Retorna probabilidade (0f..1f).
     */
    fun predict(ids: IntArray): Float {
        // input shape esperado: [1, maxLen]; o Interpreter aceita array de arrays
        val floatInput = arrayOf(ids.map { it.toFloat() }.toFloatArray())
        val output = Array(1) { FloatArray(1) }
        interpreter.run(floatInput, output)
        return output[0][0]

    }
}
