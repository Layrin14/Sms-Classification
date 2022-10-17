package com.layrin.smsclassification.ui.contact

import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.ui.common.UiState

data class ContactUiState(
    val contactId: Int,
    val contactName: String,
    val contactPhotoUri: String? = null,
    val contactPhoneNumber: String,
    val searchQuery: String? = null,
    val lookupKey: String
): UiState {
    companion object {
        fun toContactUiState(contact: Contact, query: String): ContactUiState {
            return ContactUiState(
                contactId = contact.contactId,
                contactPhoneNumber = contact.contactPhoneNumber,
                contactName = contact.contactName,
                contactPhotoUri = contact.contactPhotoUri,
                searchQuery = query,
                lookupKey = contact.contactLookUpKey
            )
        }
    }
    override val id: Int
        get() = contactId
}
