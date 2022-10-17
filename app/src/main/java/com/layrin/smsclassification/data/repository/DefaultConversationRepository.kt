package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.db.ContactDao
import com.layrin.smsclassification.data.db.ConversationDao
import com.layrin.smsclassification.data.db.MessageDao
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationAndMessage
import com.layrin.smsclassification.data.model.ConversationWithMessageAndContact
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.data.provider.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val contactProvider: ContactProvider,
    private val smsManager: SmsManager,
    private val contactDao: ContactDao
) : ConversationRepository {
    override fun getAllConversations(): PagingSource<Int, ConversationWithMessageAndContact> {
        return conversationDao.getAllConversations()
    }

    override suspend fun getSingleConversation(number: String): ConversationAndMessage? {
        return conversationDao.getSingleConversation(number)
    }

    override suspend fun insert(data: Conversation) = withContext(Dispatchers.IO) {
        conversationDao.insertConversation(data)
    }

    override suspend fun update(data: Conversation): Int =
        conversationDao.updateConversation(data)

    override suspend fun deleteAllMessage(context: Context, number: String) =
        withContext(Dispatchers.IO) {
            messageDao.deleteAllMessages(context, number)
        }

    override suspend fun delete(data: Conversation) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversation(data)
    }

    override suspend fun updateContactDataFromProvider() = withContext(Dispatchers.IO) {
        val newData = contactProvider.getContactList()
        val fromDb = contactDao.getAllContactFromDb()
        contactDao.insertAllContacts(newData)
        fromDb.forEach { contact ->
            if (!newData.contains(contact)) contactDao.deleteContact(contact)
        }
    }

    override suspend fun updateMessageData() = smsManager.updateMessageData()

    override suspend fun updateConversationLabel(label: Int, number: String) =
        withContext(Dispatchers.IO) {
            conversationDao.updateConversationLabel(label, number)
        }
}