package com.layrin.smsclassification.ui.conversation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.repository.ConversationRepository
import com.layrin.smsclassification.ui.common.SelectionManager
import com.layrin.smsclassification.ui.common.StateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val repository: ConversationRepository,
    private val selectionManager: SelectionManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<ConversationUiEvent>()

    val eventFlow: SharedFlow<ConversationUiEvent>
        get() = _eventFlow.asSharedFlow()

    val selectedConversation: List<Int>
        get() = selectionManager.selectedItems

    val isMultiSelectionModeActive: Boolean
        get() = selectionManager.isMultiSelectionModeActive

    fun updateContactData() = viewModelScope.launch {
        repository.updateContactDataFromProvider()
    }

    fun updateMessageData() = viewModelScope.launch {
        repository.updateMessageData()
    }

    fun toggleSelectedItem(position: Int, listener: (Int) -> Unit) =
        selectionManager.toggleSelectedItem(position, listener)

    fun isItemSelected(position: Int): Boolean =
        selectionManager.isItemSelected(position)

    fun clearSelectedConversation(): List<Int> = selectionManager.clearSelectedItems()

    fun onEvent(event: StateEvent) {
        when (event) {
            is ConversationStateEvent.DeleteConversationEvent -> {
                viewModelScope.launch {
                    try {
                        event.conversations.forEach { conversation ->
                            deleteConversation(conversation)
                        }
                        _eventFlow.emit(
                            ConversationUiEvent.ShowSnackBar(
                                "${event.conversations.size} conversation(s) deleted"
                            )
                        )
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            ConversationUiEvent.ShowSnackBar(e.message ?: event.errorInfo())
                        )
                    }
                }
            }
            is ConversationStateEvent.ChangeConversationLabelEvent -> {
                viewModelScope.launch {
                    try {
                        event.numbers.forEach { number ->
                            updateConversationLabel(event.label, number)
                        }
                        _eventFlow.emit(
                            ConversationUiEvent.ShowSnackBar(
                                "${event.numbers.size} conversation(s) label changed to ${event.label}"
                            )
                        )
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            ConversationUiEvent.ShowSnackBar(
                                e.message ?: event.errorInfo()
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun updateConversationLabel(labelType: LabelType, number: String) {
        when (labelType) {
            is LabelType.Fraud -> repository.updateConversationLabel(1, number)
            is LabelType.Normal -> repository.updateConversationLabel(0, number)
            is LabelType.StatusAds -> repository.updateConversationLabel(2, number)
        }
    }

    private suspend fun deleteConversation(conversation: Conversation) {
        repository.delete(conversation)
        repository.deleteAllMessage(appContext, conversation.contactPhoneNumber)
    }

    fun getAllConversation(): Flow<PagingData<ConversationUiState>> {
        return Pager(
            PagingConfig(
                pageSize = 15,
                initialLoadSize = 20,
                prefetchDistance = 25,
                maxSize = 100
            )
        ) {
            repository.getAllConversations()
        }.flow.map { pagingData ->
            pagingData.map { item ->
                ConversationUiState.toConversationUiState(item)
            }
        }.cachedIn(viewModelScope)
    }
}

sealed class ConversationUiEvent {
    data class ShowSnackBar(val message: String) : ConversationUiEvent()
}