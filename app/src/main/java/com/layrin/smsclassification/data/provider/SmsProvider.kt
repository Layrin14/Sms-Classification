package com.layrin.smsclassification.data.provider

import android.content.Context
import android.provider.Telephony
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.util.getStringValue

class SmsProvider(
    private val context: Context
) {

    fun getAllMessages(date: String): Map<String, ArrayList<Message>> {
        val smsUri = Telephony.Sms.CONTENT_URI
        val smsSelection = "date>?"
        val messages = mutableMapOf<String, ArrayList<Message>>()

        context.contentResolver.query(smsUri, null, smsSelection, arrayOf(date), "date ASC")
            .use { cursor ->
                if (cursor?.moveToFirst() == true) {
                    do {
                        val address = cursor.getStringValue("address")
                        val message = cursor.getStringValue("body")
                        val time = cursor.getStringValue("date")
                        val type = cursor.getStringValue("type")
                        val id = cursor.getStringValue("_id")
                        val readStatus = cursor.getStringValue("read")
                        val number = ContactProvider.getCleanNumber(address, context)
                        if (message.isNotEmpty()) {
                            val item = Message(
                                id.toInt(),
                                number,
                                readStatus != "0",
                                message,
                                type.toInt(),
                                time.toLong(),
                                true
                            )
                            if (messages.containsKey(number)) messages[number]?.add(item)
                            else messages[number] = arrayListOf(item)
                        }
                    } while (cursor.moveToNext())
                }
                cursor?.close()
            }
        return messages.toMap()
    }
}