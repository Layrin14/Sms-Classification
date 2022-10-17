package com.layrin.smsclassification.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.layrin.smsclassification.data.common.ModelComparator

// One-to-one relationship, one contact can only have one conversation
data class ConversationWithMessageAndContact(
    @Embedded val conversation: Conversation,
    @Relation(
        parentColumn = "message_id",
        entityColumn = "id",
        entity = Message::class
    ) val messageAndContact: MessageAndContact
): ModelComparator {
    override val id: Int
        get() = conversation.id
}
