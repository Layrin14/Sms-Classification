package com.layrin.smsclassification.data.db

import androidx.paging.PagingSource
import androidx.room.*
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.ConversationAndMessage
import com.layrin.smsclassification.data.model.ConversationWithMessageAndContact

@Dao
interface ConversationDao {

    @Transaction
    @Query("SELECT * FROM conversations " +
            "JOIN messages " +
            "ON conversations.message_id = messages.id " +
            "LEFT JOIN contacts " +
            "ON conversations.conversation_number = contacts.number " +
            "ORDER BY messages.message_time DESC")
    fun getAllConversations(): PagingSource<Int, ConversationWithMessageAndContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Update
    suspend fun updateConversation(conversation: Conversation): Int

    @Query("SELECT * FROM conversations WHERE conversation_number = :number")
    suspend fun getConversation(number: String): Conversation

    @Query("UPDATE conversations SET conversation_label = :label WHERE conversation_number = :number")
    suspend fun updateConversationLabel(label: Int, number: String)

    @Query("SELECT * FROM conversations " +
            "JOIN messages " +
            "ON conversations.message_id = messages.id " +
            "WHERE conversation_number = :number")
    suspend fun getSingleConversation(number: String): ConversationAndMessage?
}