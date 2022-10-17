package com.layrin.smsclassification.ui

import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.layrin.smsclassification.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest<T: UiState> {

    internal val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)
    internal lateinit var differ: AsyncPagingDataDiffer<T>

    @Before
    internal open fun setUp() {
        differ = AsyncPagingDataDiffer(
            diffCallback = MyDiffCallback(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )
        Dispatchers.setMain(testDispatcher)
    }

    @After
    internal open fun tearDown() {
        Dispatchers.resetMain()
    }

    inner class NoopListCallback : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }

    inner class MyDiffCallback : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(
            oldItem: T,
            newItem: T,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: T,
            newItem: T,
        ): Boolean {
            return oldItem == newItem
        }
    }
}