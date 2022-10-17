package com.layrin.smsclassification.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object ConversationTypeConverter {
    @TypeConverter
    fun arrayToJson(probability: Array<Float>?): String {
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
        return gson.toJson(probability)
    }

    @TypeConverter
    fun jsonToArray(probability: String?): Array<Float> =
        Gson().fromJson(probability, Array<Float>::class.java)
}