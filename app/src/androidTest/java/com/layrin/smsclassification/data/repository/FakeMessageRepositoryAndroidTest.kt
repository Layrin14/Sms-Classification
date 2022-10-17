package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.*
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeMessageRepositoryAndroidTest @Inject constructor(
    private val smsProvider: FakeSmsProvider,
    private val contactProvider: FakeContactProvider
) : MessageRepository {

    private val messages = mutableListOf<Message>()
    private val contacts = mutableListOf<Contact>()
    private val items = mutableListOf<MessageAndContact>()
    private val conversations = mutableListOf<Conversation>()
    private val conversationAndMessage = mutableListOf<ConversationAndMessage>()

    init {
        messages.addAll(
            smsProvider.getAllMessages().flatMap { it.value }
        )
        contacts.addAll(contactProvider.getContactList())
    }

    override suspend fun insertAllMessages(messages: List<Message>) {
        this.messages.addAll(messages)
    }

    override fun getAllPagedMessages(number: String): PagingSource<Int, MessageAndContact> {
        messages.forEach { message ->
            if (contacts.map { it.contactPhoneNumber }.contains(message.contactPhoneNumber)) {
                val item = contacts.find { it.contactPhoneNumber == message.contactPhoneNumber }
                items.add(MessageAndContact(message, item))
            }
        }
        val data =
            items.filter { messageAndContact -> messageAndContact.messages.contactPhoneNumber == number }

        return PagingSourceHelper(data)
    }

    override suspend fun insert(data: Message): Long {
        messages.add(data)
        return messages.indexOf(data).toLong()
    }

    override suspend fun updateMessageReadStatus(number: String) {
        val found = messages.find { message -> message.contactPhoneNumber == number }
        found?.let { message ->
            val data = message.copy(
                messageReadStatus = true
            )
            messages.remove(message)
            messages.add(data)
        }
    }

    override suspend fun getConversation(number: String): Conversation {
        val data = smsProvider.getAllMessages().flatMap { it.value }
        return Conversation(
            conversationLabel = 0,
            messageId = data.last().messageId,
            contactPhoneNumber = data.last().contactPhoneNumber
        )
    }

    override suspend fun delete(context: Context, data: Message): Int {
        return if (messages.remove(data)) 1 else 0
    }

    override suspend fun getAllMessages(number: String): List<Message> {
        return messages.toList()
    }

    override suspend fun updateConversation(conversation: Conversation?) {
        return
    }

    override suspend fun deleteConversation(conversation: Conversation?) {
        conversations.remove(conversation)
    }

    override suspend fun insertConversation(conversation: Conversation?) {
        conversation?.let{ conversations.add(it) }
    }

    override suspend fun getSingleMessage(id: Int): Message? {
        return messages.find { it.id == id }
    }

    override suspend fun updateMessage(data: Message) {
        messages.find { it.id == data.id }?.let {
                message -> messages.remove(message)
        }
        messages.add(data)
    }

    override suspend fun getSingleConversation(number: String): ConversationAndMessage? {
        return conversationAndMessage.find { it.conversation.contactPhoneNumber == number }
    }

    override fun getContactName(number: String?): Flow<String?> {
        return flow {
            val contact = contactProvider.getContactList().find { it.contactPhoneNumber == number }
            emit(contact?.contactName)
        }
    }
}
