package com.layrin.smsclassification.ui.message

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.model.MessageAndContact
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.provider.FakeSmsProvider
import com.layrin.smsclassification.data.repository.FakeMessageRepository
import com.layrin.smsclassification.ui.BaseViewModelTest
import com.layrin.smsclassification.ui.common.SelectionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MessageViewModelTest : BaseViewModelTest<MessageUiState>() {

    private lateinit var repository: FakeMessageRepository
    private lateinit var viewModel: MessageViewModel
    private lateinit var messages: MutableList<Message>
    private var messagesAndContacts = mutableListOf<MessageAndContact>()
    private lateinit var messageUiStates: List<MessageUiState>
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var selectionManager: SelectionManager
    private lateinit var smsProvider: FakeSmsProvider
    private lateinit var contactProvider: FakeContactProvider
    private lateinit var context: Context

    @Before
    override fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        smsProvider = FakeSmsProvider()
        contactProvider = FakeContactProvider()
        messages = mutableListOf()
        messages.addAll(smsProvider.getAllMessages().flatMap { it.value })
        messages.forEach { message ->
            if (contactProvider.getContactList().map { it.contactPhoneNumber }
                    .contains(message.contactPhoneNumber)) {
                val item = contactProvider.getContactList()
                    .find { it.contactPhoneNumber == message.contactPhoneNumber }
                messagesAndContacts.add(MessageAndContact(message, item))
            }
        }
        messageUiStates = messagesAndContacts.map {
            MessageUiState.toMessageUiState(it)
        }
        repository = FakeMessageRepository(smsProvider, contactProvider)
        selectionManager = SelectionManager()
        savedStateHandle = mock()
        whenever(savedStateHandle.get<String>("contactPhoneNumber")).thenReturn("a")
        viewModel = MessageViewModel(repository, savedStateHandle, selectionManager, context)
        super.setUp()
    }

    @Test
    fun whenGetMessage_shouldReturnFullList() = testScope.runTest {
        val expected = messageUiStates.filter { it.contactPhoneNumber == "a" }

        val job = launch {
            viewModel.messageFlow.collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()

        assertArrayEquals(
            expected.toTypedArray(),
            differ.snapshot().items.sortedBy { it.id }.toTypedArray()
        )
        job.cancel()
    }

    @Test
    fun whenDeleteMessage_shouldRemoveFromList() = testScope.runTest {
        val data = messagesAndContacts.subList(0, 2)
        val expected = data.map { MessageUiState.toMessageUiState(it) }

        viewModel.onEvent(MessageStateEvent.DeleteMessageEvent(data.map { it.messages }))

        val job = launch {
            viewModel.messageFlow.collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()
        assertFalse(differ.snapshot().items.containsAll(expected))
        job.cancel()
    }
}