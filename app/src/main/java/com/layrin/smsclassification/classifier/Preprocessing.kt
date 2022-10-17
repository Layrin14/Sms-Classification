package com.layrin.smsclassification.classifier

import android.content.Context
import javax.inject.Inject

class Preprocessing @Inject constructor(
    context: Context
) {

    private val token = "words.json"
    private var tokenData: Map<String, Int>? = null
    private val stopWords = "stopwords.json"
    private var stopWordsData: Map<String, Int>? = null
    private val fileLoader: FileLoader = FileLoader(context)

    fun tokenizer(text: String): Array<String> {
        val stopWordsList = fileLoader.loadJsonFile(stopWords)
        stopWordsData = fileLoader.jsonToMap(stopWordsList)
        val parts = text.split(" ").toMutableList()
        val iterator = parts.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (stopWordsData?.keys?.contains(item) == true) iterator.remove()
        }
        return parts.toTypedArray()
    }

    fun textToSequence(data: Array<String>): IntArray {
        val vocabList = fileLoader.loadJsonFile(token)
        tokenData = fileLoader.jsonToMap(vocabList)
        val sequence = mutableListOf<Int>()
        data.forEach {
            val index = if (tokenData?.get(it) != null) tokenData?.get(it) else 0
            if (index != null) sequence.add(index)
        }
        return sequence.toIntArray()
    }

    fun cleanText(text: String): String {
        val character = Regex("[^0-9a-z +]")
        val duplicateSpace = Regex("\\s+")
        var newText = text.lowercase()
        newText = removeRegex(newText, character)
        newText = removeRegex(newText, duplicateSpace)
        return newText
    }

    private fun removeRegex(text: String, regex: Regex): String {
        val texts = regex.findAll(text)
        var newText = text
        for (t in texts) newText = newText.replace(t.groupValues.first().toString(), " ")
        return newText.trim()
    }

    fun padSequence(data: IntArray): IntArray {
        val maxSequenceLen = 13
        return if (data.size < maxSequenceLen ) {
            val array = mutableListOf<Int>()
            array.addAll(data.asList())
            for (i in array.size until maxSequenceLen) array.add(0)
            array.toIntArray()
        } else if (data.size > maxSequenceLen) {
            data.sliceArray(0 until maxSequenceLen)
        } else data
    }


}