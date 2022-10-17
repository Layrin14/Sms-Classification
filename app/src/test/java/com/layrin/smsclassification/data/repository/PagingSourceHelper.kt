package com.layrin.smsclassification.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.layrin.smsclassification.data.common.ModelComparator
import kotlinx.coroutines.delay
import java.lang.Integer.max

private const val STARTING_KEY = 0

internal class PagingSourceHelper<T : ModelComparator>(
    private val data: List<T>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val item = state.closestItemToPosition(anchorPosition) ?: return null
        return max(STARTING_KEY, item.id - (state.config.pageSize / 2))
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val start = params.key ?: STARTING_KEY
        val range = start.until(start + params.loadSize)
        if (start != STARTING_KEY) delay(3_000L)
        return LoadResult.Page(
            data = data,
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> when (val prevKey = max(STARTING_KEY, (range.first - params.loadSize))) {
                    STARTING_KEY -> null
                    else -> prevKey
                }
            },
            nextKey = range.last + 1
        )
    }
}