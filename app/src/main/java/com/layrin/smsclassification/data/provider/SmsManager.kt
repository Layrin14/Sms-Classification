package com.layrin.smsclassification.data.provider

import android.content.SharedPreferences
import com.layrin.smsclassification.classifier.Model
import com.layrin.smsclassification.data.db.ConversationDao
import com.layrin.smsclassification.data.db.MessageDao
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmsManager @Inject constructor(
    private val contactProvider: ContactProvider,
    private val smsProvider: SmsProvider,
    private val preferences: SharedPreferences,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val model: Model,
) {

    private var index = 0

    private val messageProbability = mutableMapOf<String, Array<Float>>()

    suspend fun updateMessageData() = withContext(Dispatchers.IO) {
        index = preferences.getInt(KEY_RESUME_INDEX, 0)

        val date = preferences.getLong(KEY_RESUME_DATE, 0).toString()
        val messages = smsProvider.getAllMessages(date)
        val contactMap = contactProvider.getContactMap()

        val messageEntries = messages.entries.toTypedArray()
        val totalMessages = messageEntries.size

        for (idx in index until totalMessages) {
            val number = messageEntries[idx].component1()
            val msg = messageEntries[idx].component2()
            val label = if (contactMap.containsKey(number)) 0
            else {
                val probability = model.getPredictions(msg)
                messageProbability[number] = probability
                probability.indexOf(probability.maxOrNull())
            }
            messages[number]?.let { items ->
                val latest = items.sortedByDescending { item -> item.messageTime }
                saveConversation(
                    idx,
                    number,
                    items,
                    label,
                    latest.first().messageReadStatus,
                    messageProbability[number]
                )
            }
        }
        finish()
    }

    fun loadAndClassify(
        scope: CoroutineScope,
        done: Int,
        timeTaken: Long,
        updateProgress: (size: Int, total: Int) -> Unit,
    ) {
        index = preferences.getInt(KEY_RESUME_INDEX, 0)

        val date = preferences.getLong(KEY_RESUME_DATE, 0).toString()
        val messages = smsProvider.getAllMessages(date)
        val contactMap = contactProvider.getContactMap()

        var totalProgress = 0

        for ((_, msg) in messages) totalProgress += msg.size

        val messageEntries = messages.entries.toTypedArray()
        val totalMessages = messageEntries.size

        for (idx in index until totalMessages) {
            val number = messageEntries[idx].component1()
            val msg = messageEntries[idx].component2()
            val label = if (contactMap.containsKey(number)) 0
            else {
                val probability = model.getPredictions(msg)
                messageProbability[number] = probability
                probability.indexOf(probability.maxOrNull())
            }
            messages[number]?.let { items ->
                val latest = items.sortedByDescending { item -> item.messageTime }
                scope.launch {
                    saveConversation(
                        idx,
                        number,
                        items,
                        label,
                        latest.first().messageReadStatus,
                        messageProbability[number],
                        done,
                        timeTaken
                    )
                }
            }
            updateProgress(msg.size, totalProgress)
        }
        finish()
    }

    private suspend fun saveConversation(
        idx: Int,
        number: String,
        messages: ArrayList<Message>,
        label: Int,
        read: Boolean,
        probability: Array<Float>?,
        done: Int = 0,
        timeTaken: Long = 0L,
    ) = withContext(Dispatchers.IO) {
        if (number.isBlank()) return@withContext

        val conversation = conversationDao.getSingleConversation(number)
        if (conversation != null) {
            val data = conversation.message.copy(
                messageReadStatus = read
            )
            messageDao.updateMessage(data)
        } else {
            val data = Conversation(
                contactPhoneNumber = number,
                conversationLabel = label,
                conversationProbability = probability ?: Array(3) { value ->
                    if (value == 0) 1F else 0F
                },
                messageId = messages.sortedByDescending { it.messageTime }.first().messageId
            )
            conversationDao.insertConversation(data)
        }
        messageDao.insertAllMessages(messages)
        preferences.edit()
            .putInt(KEY_RESUME_INDEX, idx + 1)
            .putInt(KEY_DONE_COUNT, done)
            .putLong(KEY_TIME_TAKEN, timeTaken)
            .apply()
    }

    private fun finish() {
        model.close()
        messageProbability.clear()
        preferences.edit()
            .remove(KEY_RESUME_INDEX)
            .putLong(KEY_RESUME_DATE, System.currentTimeMillis())
            .apply()
    }

    companion object {
        const val KEY_RESUME_DATE = "last_date"
        const val KEY_RESUME_INDEX = "last_index"
        const val KEY_DONE_COUNT = "done_count"
        const val KEY_TIME_TAKEN = "time_taken"
    }
}