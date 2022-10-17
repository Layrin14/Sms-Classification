package com.layrin.smsclassification.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.layrin.smsclassification.data.common.ModelComparator

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "conversation_id")
    val conversationId: Int = 0,
    @ColumnInfo(name = "conversation_label")
    val conversationLabel: Int = -1,
    @ColumnInfo(name = "conversation_prob")
    val conversationProbability: Array<Float> = Array(3) { 0F },
    @ColumnInfo(name = "conversation_number")
    val contactPhoneNumber: String,
    @ColumnInfo(name = "message_id")
    val messageId: Int = -1,
) : ModelComparator {
    override val id: Int
        get() = conversationId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

        if (conversationId != other.conversationId) return false
        if (conversationLabel != other.conversationLabel) return false
        if (!conversationProbability.contentEquals(other.conversationProbability)) return false
        if (contactPhoneNumber != other.contactPhoneNumber) return false
        if (messageId != other.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        return conversationId.hashCode()
    }
}