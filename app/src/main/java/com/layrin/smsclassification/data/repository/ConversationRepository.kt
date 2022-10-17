package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationAndMessage
import com.layrin.smsclassification.data.model.ConversationWithMessageAndContact

interface ConversationRepository{
    fun getAllConversations(): PagingSource<Int, ConversationWithMessageAndContact>
    suspend fun updateConversationLabel(label: Int, number: String)
    suspend fun getSingleConversation(number: String): ConversationAndMessage?
    suspend fun deleteAllMessage(context: Context, number: String)
    suspend fun updateContactDataFromProvider()
    suspend fun updateMessageData()
    suspend fun insert(data: Conversation)
    suspend fun update(data: Conversation): Int
    suspend fun delete(data: Conversation)
}