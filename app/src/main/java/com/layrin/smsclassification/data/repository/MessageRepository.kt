package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationAndMessage
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.model.MessageAndContact
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllPagedMessages(number: String): PagingSource<Int, MessageAndContact>
    suspend fun insertAllMessages(messages: List<Message>)
    suspend fun insert(data: Message): Long
    suspend fun updateMessageReadStatus(number: String)
    suspend fun getConversation(number: String): Conversation
    suspend fun delete(context: Context, data: Message): Int
    suspend fun getAllMessages(number: String): List<Message>
    suspend fun updateConversation(conversation: Conversation?)
    suspend fun deleteConversation(conversation: Conversation?)
    suspend fun insertConversation(conversation: Conversation?)
    suspend fun getSingleMessage(id: Int): Message?
    suspend fun updateMessage(data: Message)
    suspend fun getSingleConversation(number: String): ConversationAndMessage?
    fun getContactName(number: String?): Flow<String?>
}