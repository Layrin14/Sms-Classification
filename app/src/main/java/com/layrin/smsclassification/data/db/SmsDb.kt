package com.layrin.smsclassification.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.layrin.smsclassification.data.model.*

@Database(
    entities = [Contact::class, Conversation::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConversationTypeConverter::class)
abstract class SmsDb: RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}