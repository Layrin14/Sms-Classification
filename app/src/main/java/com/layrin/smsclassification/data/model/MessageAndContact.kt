package com.layrin.smsclassification.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.layrin.smsclassification.data.common.ModelComparator

// One-to-many relationship, one contact can have many messages
data class MessageAndContact(
    @Embedded val messages: Message,
    @Relation(
        parentColumn = "message_number",
        entityColumn = "number"
    ) val contact: Contact?,
): ModelComparator {
    override val id: Int
        get() = messages.messageId

    companion object {
        fun toMessage(item: MessageAndContact) = item.messages
    }
}
