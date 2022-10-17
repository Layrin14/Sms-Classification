package com.layrin.smsclassification.ui.conversation

import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationWithMessageAndContact
import com.layrin.smsclassification.ui.common.UiState

data class ConversationUiState(
    val conversationId: Int,
    val conversationLabel: Int,
    val conversationReadStatus: Boolean,
    val contactPhoneNumber: String,
    val messageId: Int,
    val conversationTime: Long,
    val conversationProbability: Array<Float>,
    val lastMessage: String,
    val contactName: String? = null,
    val contactPhotoUri: String? = null,
    val contactId: Int? = null
): UiState {
    companion object {
        fun toConversationUiState(
            item: ConversationWithMessageAndContact
        ): ConversationUiState {
            return ConversationUiState(
                conversationId = item.conversation.conversationId,
                conversationLabel = item.conversation.conversationLabel,
                conversationReadStatus = item.messageAndContact.messages.messageReadStatus,
                contactPhoneNumber = item.conversation.contactPhoneNumber,
                messageId = item.messageAndContact.messages.messageId,
                lastMessage = item.messageAndContact.messages.messageText,
                conversationTime = item.messageAndContact.messages.messageTime,
                conversationProbability = item.conversation.conversationProbability,
                contactPhotoUri = item.messageAndContact.contact?.contactPhotoUri,
                contactName = item.messageAndContact.contact?.contactName,
                contactId = item.messageAndContact.contact?.contactId
            )
        }
        fun toConversation(
            item: ConversationUiState
        ): Conversation {
            return Conversation(
                conversationId = item.conversationId,
                conversationLabel = item.conversationLabel,
                conversationProbability = item.conversationProbability,
                contactPhoneNumber = item.contactPhoneNumber,
                messageId = item.messageId
            )
        }
    }
    override val id: Int
        get() = conversationId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationUiState

        if (conversationId != other.conversationId) return false
        if (conversationLabel != other.conversationLabel) return false
        if (conversationReadStatus != other.conversationReadStatus) return false
        if (contactPhoneNumber != other.contactPhoneNumber) return false
        if (messageId != other.messageId) return false
        if (conversationTime != other.conversationTime) return false
        if (!conversationProbability.contentEquals(other.conversationProbability)) return false
        if (lastMessage != other.lastMessage) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
