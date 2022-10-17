package com.layrin.smsclassification.ui.message

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.repository.MessageRepository
import com.layrin.smsclassification.ui.common.SelectionManager
import com.layrin.smsclassification.ui.common.StateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MessageRepository,
    savedStateHandle: SavedStateHandle,
    private val selectionManager: SelectionManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private var number: String? = null

    private val _eventFlow = MutableSharedFlow<MessageUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var _messageFlow: Flow<PagingData<MessageUiState>>? = null
    val messageFlow get() = checkNotNull(_messageFlow)

    val senderName
        get() = repository.getContactName(number)
            .stateIn(viewModelScope, SharingStarted.Eagerly, number)

    val selectedMessage: List<Int>
        get() = selectionManager.selectedItems

    val isMultiSelectionModeActive: Boolean
        get() = selectionManager.isMultiSelectionModeActive

    fun toggleSelectedItem(position: Int, listener: (Int) -> Unit) =
        selectionManager.toggleSelectedItem(position, listener)

    fun isItemSelected(position: Int): Boolean =
        selectionManager.isItemSelected(position)

    fun clearSelectedMessages(): List<Int> = selectionManager.clearSelectedItems()

    init {
        savedStateHandle.get<String>("contactPhoneNumber")?.let { phoneNumber ->
            if (phoneNumber.isNotBlank()) {
                _messageFlow = getMessage(phoneNumber)
                this.number = phoneNumber
                updateReadStatus(phoneNumber)
            }
        }
    }

    private fun updateReadStatus(number: String) = viewModelScope.launch {
        repository.updateMessageReadStatus(number)
    }

    private fun getMessage(number: String): Flow<PagingData<MessageUiState>> {
        val pager = Pager(
            PagingConfig(
                pageSize = 15,
                initialLoadSize = 25,
                prefetchDistance = 60,
                maxSize = 180
            )
        ) {
            repository.getAllPagedMessages(number)
        }
        return pager.flow.map { pagingData ->
            pagingData.map { data ->
                MessageUiState.toMessageUiState(data)
            }
        }.cachedIn(viewModelScope)
    }

    fun onEvent(event: StateEvent) {
        when (event) {
            is MessageStateEvent.DeleteMessageEvent -> {
                viewModelScope.launch {
                    try {
                        event.messages.forEach { message ->
                            val result = deleteMessage(message)
                            if (result == 1) {
                                updateConversationData(message.contactPhoneNumber)
                            }
                        }
                        _eventFlow.emit(
                            MessageUiEvent.ShowSnackBar(
                                "${event.messages.size} message(s) deleted"
                            )
                        )
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            MessageUiEvent.ShowSnackBar(
                                e.message ?: event.errorInfo()
                            )
                        )
                    }
                }
            }
            is MessageStateEvent.SendMessageEvent -> {
                viewModelScope.launch {
                    try {
                        _eventFlow.emit(MessageUiEvent.SendMessage)
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            MessageUiEvent.ShowSnackBar(
                                e.message ?: event.errorInfo()
                            )
                        )
                    }
                }
            }
        }
    }

    fun updateConversationData(message: Message) = viewModelScope.launch(Dispatchers.IO) {
        val messageId = repository.insert(message).toInt()
        val conversation = repository.getSingleConversation(message.contactPhoneNumber)
        val data = conversation?.conversation?.copy(
            messageId = messageId
        ) ?: Conversation(
            conversationLabel = 0,
            messageId = messageId,
            contactPhoneNumber = message.contactPhoneNumber,
            conversationProbability = arrayOf(1F, 0F, 0F)
        )
        if (conversation?.conversation != null) repository.updateConversation(data)
        else repository.insertConversation(data)
    }

    private fun updateConversationData(number: String) = viewModelScope.launch {
        val messages = repository.getAllMessages(number)
        val conversation =
            repository.getConversation(number)
        if (messages.isNotEmpty()) {
            val latest = messages.sortedByDescending { it.messageTime }
            val updatedConversation = conversation.copy(
                conversationId = conversation.conversationId,
                conversationProbability = conversation.conversationProbability,
                messageId = latest.first().id
            )
            repository.updateConversation(updatedConversation)
        } else {
            repository.deleteConversation(conversation)
        }
    }

    private suspend fun deleteMessage(message: Message): Int =
        repository.delete(appContext, message)
}

sealed class MessageUiEvent {
    data class ShowSnackBar(val message: String) : MessageUiEvent()
    object SendMessage : MessageUiEvent()
}