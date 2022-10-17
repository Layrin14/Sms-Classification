package com.layrin.smsclassification.ui.message

import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.model.MessageAndContact
import com.layrin.smsclassification.ui.common.UiState

data class MessageUiState(
    val messageId: Int,
    val contactPhoneNumber: String,
    val messageText: String,
    val messageType: Int,
    val messageTime: Long,
    val messageSentStatus: Boolean,
    val contactName: String? = null
): UiState {
    companion object {
        fun toMessageUiState(item: MessageAndContact): MessageUiState =
            MessageUiState(
                messageId = item.messages.messageId,
                contactPhoneNumber = item.messages.contactPhoneNumber,
                messageText = item.messages.messageText,
                messageType = item.messages.messageType,
                messageTime = item.messages.messageTime,
                messageSentStatus = item.messages.messageSentStatus,
                contactName = item.contact?.contactName
            )
        fun toMessage(item: MessageUiState): Message =
            Message(
                messageId = item.messageId,
                contactPhoneNumber = item.contactPhoneNumber,
                messageTime = item.messageTime,
                messageText = item.messageText,
                messageType = item.messageType,
                messageSentStatus = item.messageSentStatus
            )
    }

    override val id: Int
        get() = messageId
}
