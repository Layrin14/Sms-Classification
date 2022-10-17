package com.layrin.smsclassification.data.repository

import kotlinx.coroutines.CoroutineScope

interface StartScreenRepository {
    val startTime: Long
    fun loadAndClassify(
        scope: CoroutineScope,
        done: Int,
        timeTaken: Long,
        updateProgress: (size: Int, total: Int) -> Unit
    )
    suspend fun getContactList()
}