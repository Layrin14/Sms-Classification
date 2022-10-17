package com.layrin.smsclassification.data.repository

import android.content.SharedPreferences
import com.layrin.smsclassification.data.db.ContactDao
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.data.provider.SmsManager
import com.layrin.smsclassification.data.provider.SmsManager.Companion.KEY_TIME_TAKEN
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class DefaultStartScreenRepository @Inject constructor(
    private val contactProvider: ContactProvider,
    private val contactDao: ContactDao,
    preferences: SharedPreferences,
    private val smsManager: SmsManager
) : StartScreenRepository {

    override val startTime = System.currentTimeMillis() - preferences.getLong(KEY_TIME_TAKEN, 0)

    override suspend fun getContactList() {
        val data = contactProvider.getContactList()
        contactDao.insertAllContacts(data)
    }

    override fun loadAndClassify(
        scope: CoroutineScope,
        done: Int,
        timeTaken: Long,
        updateProgress: (size: Int, total: Int) -> Unit,
    ) {
        smsManager.loadAndClassify(scope, done, timeTaken, updateProgress)
    }
}