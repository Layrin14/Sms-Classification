package com.layrin.smsclassification.data.repository

import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.Contact

interface ContactRepository {

    fun getContacts(query: String): PagingSource<Int, Contact>

    suspend fun insertAllContacts(contacts: Array<Contact>)

    fun getContactMap(): Map<String, String>
}