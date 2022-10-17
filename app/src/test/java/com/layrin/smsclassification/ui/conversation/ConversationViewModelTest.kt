package com.layrin.smsclassification.ui.conversation

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.layrin.smsclassification.data.model.*
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import com.layrin.smsclassification.data.repository.ConversationRepository
import com.layrin.smsclassification.data.repository.FakeConversationRepository
import org.junit.Assert.*

import com.layrin.smsclassification.ui.BaseViewModelTest
import com.layrin.smsclassification.ui.common.SelectionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ConversationViewModelTest : BaseViewModelTest<ConversationUiState>() {

    private lateinit var repository: ConversationRepository
    private lateinit var viewModel: ConversationViewModel
    private lateinit var conversationUiState: List<ConversationUiState>
    private lateinit var items: MutableList<MessageAndContact>
    private lateinit var finalData: MutableList<ConversationWithMessageAndContact>
    private lateinit var selectionManager: SelectionManager
    private lateinit var smsProvider: FakeSmsProvider
    private lateinit var contactProvider: FakeContactProvider
    private lateinit var context: Context
    private lateinit var set: MutableSet<ConversationUiState>

    @Before
    override fun setUp() {
        set = mutableSetOf()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        smsProvider = FakeSmsProvider()
        contactProvider = FakeContactProvider()
        finalData = mutableListOf()
        val messages = mutableListOf<Message>()
        val conversations = mutableListOf<Conversation>()
        items = mutableListOf()
        val contacts = mutableListOf<Contact>()
        contacts.addAll(contactProvider.getContactList())
        contacts.shuffle()
        messages.addAll(
            smsProvider.getAllMessages().flatMap { it.value }
        )
        var index = 0
        smsProvider.getAllMessages().forEach { (number, msg) ->
            val conversation = Conversation(
                conversationId = index,
                conversationLabel = index % 3,
                conversationProbability = arrayOf(
                    index.toFloat(), index.toFloat() + 1, index.toFloat() + 2
                ),
                contactPhoneNumber = number,
                messageId = msg.last().id,
            )
            conversations.add(conversation)
            messages.addAll(msg)
            index++
            if (contacts.map { it.contactPhoneNumber }.contains(number)) {
                items.add(MessageAndContact(
                    msg.last(),
                    contacts.find { it.contactPhoneNumber == number })
                )
            }
        }
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
        conversationUiState = finalData.map { item ->
            ConversationUiState.toConversationUiState(item)
        }
        selectionManager = SelectionManager()
        repository = FakeConversationRepository(contactProvider, smsProvider)
        viewModel = ConversationViewModel(repository, selectionManager, context)
        super.setUp()
    }

    @Test
    fun whenGetConversationList_shouldReturnArrayOfConversation() = testScope.runTest {
        val job = launch {
            viewModel.getAllConversation().collectLatest {
                differ.submitData(it)
            }
        }
        advanceUntilIdle()
        job.cancel()
        set.addAll(differ.snapshot().items)
        assertArrayEquals(
            conversationUiState.toTypedArray(),
            set.toTypedArray()
        )
    }

    @Test
    fun whenDeleteConversation_shouldRemoveFromList() = testScope.runTest {
        val data = finalData.subList(0, 2)
        val deletedItem = data.map { ConversationUiState.toConversationUiState(it) }
        viewModel.onEvent(ConversationStateEvent.DeleteConversationEvent(data.map { it.conversation }))

        val job = launch {
            viewModel.getAllConversation().collectLatest {
                differ.submitData(it)
            }
        }
        advanceUntilIdle()
        assertFalse(differ.snapshot().items.containsAll(deletedItem))
        job.cancel()
    }

    @Test
    fun whenSuccessDeleteConversation_shouldEmitSuccessMessage() = testScope.runTest {
        val data = finalData.subList(0, 2)
        viewModel.onEvent(ConversationStateEvent.DeleteConversationEvent(data.map { it.conversation }))

        viewModel.eventFlow.test {
            val receivedEvent = awaitItem() as ConversationUiEvent.ShowSnackBar
            assertEquals("2 conversation(s) deleted", receivedEvent.message)
        }
    }

    @Test
    fun whenSuccessChangeConversationLabel_shouldEmitSuccessMessage() = testScope.runTest {
        val data = finalData.filter { it.conversation.conversationLabel == 0 }.subList(0, 3)
        viewModel.onEvent(
            ConversationStateEvent.ChangeConversationLabelEvent(
                LabelType.Fraud,
                data.map { it.conversation.contactPhoneNumber }
            )
        )

        viewModel.eventFlow.test {
            val receivedEvent = awaitItem() as ConversationUiEvent.ShowSnackBar
            assertEquals(
                "3 conversation(s) label changed to ${LabelType.Fraud}",
                receivedEvent.message
            )
        }
    }

    @Test
    fun whenChangeConversationLabel_shouldUpdateLabel() = testScope.runTest {
        val data = finalData.filter { it.conversation.conversationLabel == 0 }.subList(0, 2)
        val conversations = arrayListOf<ConversationUiState>()
        val before = data.map { ConversationUiState.toConversationUiState(it) }
        val after = before.map {
            it.copy(
                conversationLabel = 1
            )
        }
        viewModel.onEvent(
            ConversationStateEvent.ChangeConversationLabelEvent(
                LabelType.Fraud, before.map { it.contactPhoneNumber }
            )
        )
        val job = launch {
            viewModel.getAllConversation().collectLatest {
                differ.submitData(it)
            }
        }
        advanceUntilIdle()
        conversations.addAll(differ.snapshot().items)
        assertFalse(conversations.filter { it.conversationLabel == 0 }.containsAll(before))
        assertTrue(conversations.filter { it.conversationLabel == 1 }.containsAll(after))
        job.cancel()
    }
}