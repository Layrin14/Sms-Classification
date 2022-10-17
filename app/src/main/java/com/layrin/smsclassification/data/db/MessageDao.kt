package com.layrin.smsclassification.data.db

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.*
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.model.MessageAndContact
import com.layrin.smsclassification.util.deleteMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Transaction
    @Query("SELECT * FROM messages " +
            "LEFT JOIN contacts " +
            "ON messages.message_number = contacts.number " +
            "WHERE messages.message_number = :number " +
            "ORDER BY messages.message_time DESC")
    fun getAllPagedMessages(number: String): PagingSource<Int, MessageAndContact>

    @Query("SELECT contact_name FROM contacts WHERE number = :number")
    fun getContactName(number: String?): Flow<String?>

    @Query("DELETE FROM messages WHERE message_number = :number")
    suspend fun deleteAllMessages(number: String)

    @Query("SELECT * FROM messages WHERE message_number = :number")
    suspend fun getAllMessages(number: String): List<Message>

    suspend fun deleteAllMessages(context: Context, number: String) {
        deleteAllMessages(number)
        getAllMessages(number).forEach { message ->
            context.deleteMessage(message.id)
        }
    }

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getSingleMessage(id: Int): Message?

    @Query("UPDATE messages SET message_read_status = 1 WHERE message_number = :number")
    suspend fun updateMessageReadStatus(number: String)

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message): Int

    suspend fun deleteMessage(context: Context, message: Message): Int {
        context.deleteMessage(message.id)
        return deleteMessage(message)
    }
}