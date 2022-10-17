package com.layrin.smsclassification.data.repository

import androidx.paging.PagingSource
import com.layrin.smsclassification.data.db.ContactDao
import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.provider.ContactProvider
import javax.inject.Inject

class DefaultContactRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val contactProvider: ContactProvider,
) : ContactRepository {

    override suspend fun insertAllContacts(contacts: Array<Contact>) =
        contactDao.insertAllContacts(contacts)

    override fun getContacts(query: String): PagingSource<Int, Contact> =
        contactDao.getContacts("$query%", "% $query%")

    override fun getContactMap(): Map<String, String> {
        return contactProvider.getContactMap()
    }
}