package com.layrin.smsclassification.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.layrin.smsclassification.data.repository.ContactRepository
import com.layrin.smsclassification.ui.common.StateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: ContactRepository,
) : ViewModel() {

    private var _contactFlow = getContacts()

    val contactFlow: Flow<PagingData<ContactUiState>>
        get() = _contactFlow

    fun onEvent(event: StateEvent) {
        when (event) {
            is ContactStateEvent.SearchContactEvent -> {
                try {
                    _contactFlow = getContacts(event.searchQuery)
                } catch (e: Exception) {
                    println(e.message ?: event.errorInfo())
                }
            }
        }
    }

    private fun getContacts(query: String = ""): Flow<PagingData<ContactUiState>> {
        val pager = Pager(
            PagingConfig(
                pageSize = 15,
                initialLoadSize = 25,
                prefetchDistance = 30,
                maxSize = 120
            )
        ) { repository.getContacts(query) }
        return pager.flow.map { pagingData ->
            pagingData.map { contact ->
                ContactUiState.toContactUiState(contact, query)
            }
        }.cachedIn(viewModelScope)
    }
}