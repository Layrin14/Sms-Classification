package com.layrin.smsclassification.classifier

import android.content.Context
import com.layrin.smsclassification.data.model.Message
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class Model @Inject constructor(
    private val context: Context,
    private val preprocessing: Preprocessing,
) {
    private val loadModel = loadModelFile()
    private val model = Interpreter(loadModel, Interpreter.Options())

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getPrediction(message: Message) = getPredictions(arrayListOf(message))

    fun getPredictions(messages: ArrayList<Message>): Array<Float> {
        val probability = Array(3) { 0F }

        for (i in 0 until messages.size) {
            var message = messages[i].messageText.lowercase().trim()
            message = preprocessing.cleanText(message)
            val token = preprocessing.tokenizer(message)
            val sequence = preprocessing.textToSequence(token)
            val pad = preprocessing.padSequence(sequence)

            val input = arrayOf(pad.map { it.toFloat() }.toFloatArray())

            val output = arrayOf(FloatArray(3))
            model.run(input, output)

            for (prob in 0..2) {
                if (probability[prob] > output[0][prob]) continue
                else probability[prob] = output[0][prob]
            }
        }
        return probability
    }

    fun close() {
        model.close()
        loadModel.clear()
    }
}