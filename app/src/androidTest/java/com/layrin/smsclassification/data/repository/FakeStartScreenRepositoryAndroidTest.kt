package com.layrin.smsclassification.data.repository

import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class FakeStartScreenRepositoryAndroidTest @Inject constructor(
    private val smsProvider: FakeSmsProvider,
    private val contactProvider: FakeContactProvider,
) : StartScreenRepository {
    private val contacts = mutableListOf<Contact>()
    private val _messages = mutableListOf<Message>()
    override val startTime: Long
        get() = System.currentTimeMillis()

    override fun loadAndClassify(
        scope: CoroutineScope,
        done: Int,
        timeTaken: Long,
        updateProgress: (size: Int, total: Int) -> Unit,
    ) {
        val data = smsProvider.getAllMessages().flatMap { it.value }
        _messages.addAll(
            data
        )
    }

    override suspend fun getContactList() {
        val newData = contactProvider.getContactList()
        newData.forEach { contact ->
            if (!contacts.contains(contact)) contacts.add(contact)
        }
    }
}