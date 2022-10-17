package com.layrin.smsclassification.ui.conversation

import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.ui.common.StateEvent

sealed class ConversationStateEvent : StateEvent {
    data class DeleteConversationEvent(
        val conversations: List<Conversation>,
    ) : ConversationStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting conversation."
        }
    }

    data class ChangeConversationLabelEvent(
        val label: LabelType,
        val numbers: List<String>
    ) : ConversationStateEvent() {
        override fun errorInfo(): String {
            return "Error changing conversation label."
        }
    }
}