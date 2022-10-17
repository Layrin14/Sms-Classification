package com.layrin.smsclassification.ui.contact

import com.layrin.smsclassification.ui.common.StateEvent

sealed class ContactStateEvent : StateEvent {
    data class SearchContactEvent(
        val searchQuery: String
    ) : ContactStateEvent() {
        override fun errorInfo(): String {
            return "Error getting list of contacts."
        }
    }
}