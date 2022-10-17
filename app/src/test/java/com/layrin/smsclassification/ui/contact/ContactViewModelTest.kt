package com.layrin.smsclassification.ui.contact

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.layrin.smsclassification.data.model.Contact
import com.layrin.smsclassification.data.provider.FakeContactProvider
import com.layrin.smsclassification.data.repository.FakeContactRepository
import com.layrin.smsclassification.ui.BaseViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ContactViewModelTest : BaseViewModelTest<ContactUiState>() {
    private lateinit var repository: FakeContactRepository
    private lateinit var viewModel: ContactViewModel
    private lateinit var data: Array<Contact>
    private lateinit var contactProvider: FakeContactProvider

    @Before
    override fun setUp() {
        contactProvider = FakeContactProvider()
        data = contactProvider.getContactList()
        repository = FakeContactRepository(contactProvider)
        viewModel = ContactViewModel(repository)
        super.setUp()
    }

    @Test
    fun whenGetContactList_ifContactNotEmpty_shouldReturnFullList() = testScope.runTest {
        repository.insertAllContacts(data)
        val expected = data.map {
            ContactUiState.toContactUiState(it, "")
        }
        val job = launch {
            viewModel.contactFlow.collectLatest {
                differ.submitData(it)
            }
        }
        advanceUntilIdle()
        assertEquals(expected, differ.snapshot().items)
        job.cancel()
    }

    @Test
    fun whenSearchContact_ifQueryNotFound_shouldReturnEmptyList() = testScope.runTest {
        repository.insertAllContacts(data)
        viewModel.onEvent(ContactStateEvent.SearchContactEvent("sally"))
        val job = launch {
            viewModel.contactFlow.collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()
        assertEquals(emptyList<ContactUiState>(), differ.snapshot().items)
        job.cancel()
    }

    @Test
    fun whenSearchContact_ifQueryFound_shouldReturnListWithCorrespondingContacts() =
        testScope.runTest {
            repository.insertAllContacts(data)
            val query = "2"
            val expected = data.map {
                ContactUiState.toContactUiState(it, query)
            }.filter { it.contactName.contains(query) || it.contactPhoneNumber.contains(query) }
            viewModel.onEvent(ContactStateEvent.SearchContactEvent(query))
            val job = launch {
                viewModel.contactFlow.collectLatest {
                    differ.submitData(it)
                }
            }

            advanceUntilIdle()
            assertEquals(expected, differ.snapshot().items)
            job.cancel()
        }
}