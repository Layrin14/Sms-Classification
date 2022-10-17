package com.layrin.smsclassification.data.repository

import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.provider.FakeContactProvider

class FakeContactRepository(
    private val dataProvider: FakeContactProvider
): ContactRepository {

    private val contactItem = mutableListOf<Contact>()

    override fun getContactMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        dataProvider.getContactList().forEach { contact ->
            map[contact.contactPhoneNumber] = contact.contactName
        }
        return map
    }

    override fun getContacts(query: String): PagingSource<Int, Contact> {
        return if (query.isBlank()) {
            PagingSourceHelper(contactItem)
        } else {
            val data = contactItem.filter {
                it.contactName.contains(query) || it.contactPhoneNumber.contains(query)
            }
            PagingSourceHelper(data)
        }
    }

    override suspend fun insertAllContacts(contacts: Array<Contact>) {
        contactItem.addAll(contacts)
    }
}