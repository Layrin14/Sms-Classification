package com.layrin.smsclassification.classifier

import android.content.Context
import org.json.JSONObject
import java.io.IOException

class FileLoader(
    private val context: Context
) {

    internal fun loadJsonFile(file: String): String? {
        val json: String?
        try {
            val inputStream = context.assets.open(file)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return json
    }

    internal fun jsonToMap(file: String?): Map<String, Int> {
        val json = file?.let { JSONObject(it) }
        val iterator: Iterator<String>? = json?.keys()
        val data: MutableMap<String, Int> = mutableMapOf()
        while (iterator?.hasNext() == true) {
            val key = iterator.next()
            data[key] = json.get(key) as Int
        }
        return data.toMap()
    }
}