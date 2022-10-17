package com.layrin.smsclassification.data.repository

import android.content.Context
import androidx.paging.PagingSource
import com.layrin.smsclassification.data.model.*
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import javax.inject.Inject

class FakeConversationRepositoryAndroidTest @Inject constructor(
    smsProvider: FakeSmsProvider,
    private val contactProvider: FakeContactProvider,
) : ConversationRepository {
    private val messages = mutableListOf<Message>()
    private val conversations = mutableListOf<Conversation>()
    private val items = mutableListOf<MessageAndContact>()
    private val contacts = mutableListOf<Contact>()
    private val finalData = mutableListOf<ConversationWithMessageAndContact>()

    init {
        contacts.addAll(contactProvider.getContactList())
        contacts.shuffle()
        var index = 0
        smsProvider.getAllMessages().forEach { (number, msg) ->
            conversations.addAll(
                msg.map { Conversation(
                    conversationId = index,
                    conversationLabel = index % 3,
                    conversationProbability = arrayOf(
                        index.toFloat(), index.toFloat() + 1, index.toFloat() + 2
                    ),
                    contactPhoneNumber = number,
                    messageId = it.messageId,
                ) }
            )
            messages.addAll(msg)
            index++
            items.addAll(
                msg.map {
                    MessageAndContact(
                        messages = it,
                        contact = null)
                }
            )
        }
    }

    override suspend fun insert(data: Conversation) {
        conversations.add(data)
    }

    override suspend fun update(data: Conversation): Int {
        conversations.find { it.conversationId == data.conversationId }?.let { conversation ->
            conversations.remove(conversation)
        }
        conversations.add(data)
        return conversations.indexOf(data)
    }

    override suspend fun delete(data: Conversation) {
        conversations.remove(data)
    }

    override suspend fun updateConversationLabel(label: Int, number: String) {
        conversations.find { it.contactPhoneNumber == number }?.let { item ->
            val newData = item.copy(
                conversationLabel = label
            )
            val index = conversations.indexOf(item)
            conversations.remove(item)
            conversations.add(index, newData)
        }

    }

    override fun getAllConversations(): PagingSource<Int, ConversationWithMessageAndContact> {
        items.forEach { item ->
            val found = conversations.find {
                it.messageId == item.messages.id
            }
            if (found != null) {
                val conversationWithMessageAndContact = ConversationWithMessageAndContact(
                    found,
                    item
                )
                finalData.add(conversationWithMessageAndContact)
            }
        }
        return PagingSourceHelper(finalData)
    }

    override suspend fun deleteAllMessage(context: Context, number: String) {
        messages.removeAll { message ->
            message.contactPhoneNumber == number
        }
    }

    override suspend fun updateContactDataFromProvider() {
        val newData = contactProvider.getContactList()
        contacts.forEach { contact ->
            if (!newData.contains(contact)) contacts.remove(contact)
        }
    }

    override suspend fun updateMessageData() {
        return
    }

    override suspend fun getSingleConversation(number: String): ConversationAndMessage? {
        val message =
            messages.sortedByDescending { it.messageTime }.find { it.contactPhoneNumber == number }
        val conversation = conversations.find { it.contactPhoneNumber == number }
        return if (conversation?.messageId == message?.id)
            conversation?.let { conv ->
                message?.let { msg ->
                    ConversationAndMessage(conv, msg)
                }
            }
        else null
    }
}