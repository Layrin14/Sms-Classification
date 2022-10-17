package com.layrin.smsclassification.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.layrin.smsclassification.data.common.ModelComparator

// One-to-many relationship, one conversation can have many messages
// But since we only need the last message (only one) so this turn into one-to-one
data class ConversationAndMessage(
    @Embedded val conversation: Conversation,
    @Relation(
        parentColumn = "message_id",
        entityColumn = "id",
        entity = Message::class
    ) val message: Message
): ModelComparator {
    companion object {
        fun toConversation(item: ConversationAndMessage) = Conversation(
            conversationId = item.conversation.conversationId,
            conversationLabel = item.conversation.conversationLabel,
            conversationProbability = item.conversation.conversationProbability,
            contactPhoneNumber = item.conversation.contactPhoneNumber,
            messageId = item.conversation.messageId
        )
    }
    override val id: Int
        get() = conversation.conversationId
}
