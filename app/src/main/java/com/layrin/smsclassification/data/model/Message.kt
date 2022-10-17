package com.layrin.smsclassification.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.layrin.smsclassification.data.common.ModelComparator

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val messageId: Int = 0,
    @ColumnInfo(name = "message_number")
    val contactPhoneNumber: String,
    @ColumnInfo(name = "message_read_status")
    val messageReadStatus: Boolean = false,
    @ColumnInfo(name = "message_text")
    val messageText: String,
    @ColumnInfo(name = "message_type")
    val messageType: Int,
    @ColumnInfo(name = "message_time")
    val messageTime: Long,
    @ColumnInfo(name = "message_sent_status")
    val messageSentStatus: Boolean = false,
) : ModelComparator {
    override val id: Int
        get() = messageId
}
