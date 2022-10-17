package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.db.ConversationDao
import com.layrin.smsclassification.data.db.MessageDao
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationAndMessage
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.model.MessageAndContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultMessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
) : MessageRepository {

    override suspend fun insertAllMessages(messages: List<Message>) = withContext(Dispatchers.IO) {
        messageDao.insertAllMessages(messages)
    }

    override suspend fun getSingleMessage(id: Int): Message? =
        messageDao.getSingleMessage(id)

    override fun getAllPagedMessages(number: String): PagingSource<Int, MessageAndContact> =
        messageDao.getAllPagedMessages(number)

    override fun getContactName(number: String?): Flow<String?> {
        return messageDao.getContactName(number)
    }

    override suspend fun updateMessage(data: Message) = withContext(Dispatchers.IO) {
        messageDao.updateMessage(data)
    }

    override suspend fun getAllMessages(number: String): List<Message> =
        messageDao.getAllMessages(number)

    override suspend fun updateConversation(conversation: Conversation?) =
        withContext(Dispatchers.IO) {
            conversation?.let { data ->
                conversationDao.updateConversation(data)
            }
            return@withContext
        }

    override suspend fun deleteConversation(conversation: Conversation?) =
        withContext(Dispatchers.IO) {
            conversation?.let { data ->
                conversationDao.deleteConversation(data)
            }
            return@withContext
        }

    override suspend fun insertConversation(conversation: Conversation?) =
        withContext(Dispatchers.IO) {
            conversation?.let { data ->
                conversationDao.insertConversation(data)
            }
            return@withContext
        }

    override suspend fun getConversation(number: String): Conversation =
        conversationDao.getConversation(number)

    override suspend fun getSingleConversation(number: String): ConversationAndMessage? {
        return conversationDao.getSingleConversation(number)
    }

    override suspend fun insert(data: Message): Long = messageDao.insert(data)

    override suspend fun updateMessageReadStatus(number: String) = withContext(Dispatchers.IO) {
        messageDao.updateMessageReadStatus(number)
    }

    override suspend fun delete(context: Context, data: Message): Int =
        messageDao.deleteMessage(context, data)
}