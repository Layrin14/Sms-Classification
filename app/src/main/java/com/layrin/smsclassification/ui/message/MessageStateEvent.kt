package com.layrin.smsclassification.ui.message

import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.ui.common.StateEvent

sealed class MessageStateEvent: StateEvent {
    data class DeleteMessageEvent(
        val messages: List<Message>
    ): MessageStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting message."
        }
    }

    object SendMessageEvent: MessageStateEvent() {
        override fun errorInfo(): String {
            return "Error sending the message."
        }
    }
}
