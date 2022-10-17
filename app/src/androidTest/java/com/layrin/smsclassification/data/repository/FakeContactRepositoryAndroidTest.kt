package com.layrin.smsclassification.data.repository

import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.provider.FakeContactProvider
import javax.inject.Inject

class FakeContactRepositoryAndroidTest @Inject constructor(
    private val provider: FakeContactProvider
): ContactRepository {

    private val contactItem = mutableListOf<Contact>()

    init {
        contactItem.addAll(provider.getContactList())
    }

    override fun getContactMap(): Map<String, String> {
        return provider.getContactMap()
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