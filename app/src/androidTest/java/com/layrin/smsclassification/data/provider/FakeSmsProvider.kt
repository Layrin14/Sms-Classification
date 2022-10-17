package com.layrin.smsclassification.data.provider

import com.layrin.smsclassification.data.model.Message

class FakeSmsProvider {
    fun getAllMessages(): Map<String, ArrayList<Message>> {
        val data = mutableMapOf<String, ArrayList<Message>>()

        ('a'..'l').forEachIndexed { index, c ->
            val item = Message(
                messageId = (index + 1),
                contactPhoneNumber = if (index % 2 == 0) "a" else c.toString(),
                messageText = c.toString(),
                messageType = if (index % 2 == 0) 0 else 1,
                messageTime = index.toLong(),
                messageSentStatus = true
            )
            if (data.containsKey("a")) data["a"]?.add(item)
            else data["a"] = arrayListOf(item)
        }

        return data.toMap()
    }
}