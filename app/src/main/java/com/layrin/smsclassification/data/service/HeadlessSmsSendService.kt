package com.layrin.smsclassification.data.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.repository.MessageRepository
import com.layrin.smsclassification.ui.message.MessageFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HeadlessSmsSendService : Service() {

    private val scope = SupervisorJob()
    private val job = CoroutineScope(Dispatchers.IO + scope)

    @Inject
    lateinit var messageRepository: MessageRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action !in arrayOf(
                Intent.ACTION_SENDTO,
                TelephonyManager.ACTION_RESPOND_VIA_MESSAGE
            )
        ) return super.onStartCommand(intent, flags, startId)

        val extras = intent?.extras ?: return super.onStartCommand(intent, flags, startId)
        val messages = extras.getString(Intent.EXTRA_TEXT) ?: extras.getString("sms_body")
        val base = intent.data?.schemeSpecificPart
        val position = base?.indexOf('?')
        val recipient = if (position == -1) base
        else position?.let { base.substring(0, it) }

        if (TextUtils.isEmpty(recipient) || TextUtils.isEmpty(messages))
            return super.onStartCommand(
                intent,
                flags,
                startId
            )

        val number = extras.getString(Intent.EXTRA_PHONE_NUMBER)
        val address = if (number == null) TextUtils.split(recipient, ";") else arrayOf(number)
        address.forEach { phoneNumber ->
            val settings = Settings().apply {
                useSystemSending = true
                deliveryReports = true
                sendLongAsMms = false
            }
            settings.apply {

                if (packageName != Telephony.Sms.getDefaultSmsPackage(this@HeadlessSmsSendService)) {
                    return@apply
                }

                if (ActivityCompat.checkSelfPermission(this@HeadlessSmsSendService, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED) {
                    return@apply
                }

                val subscriptionManager =
                    this@HeadlessSmsSendService.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this@HeadlessSmsSendService)

                subscriptionId = if (subscriptionManager.activeSubscriptionInfoCount == 2) {
                    sharedPreferences.getInt(
                        MessageFragment.SELECTED_SIM_KEY,
                        subscriptionManager.activeSubscriptionInfoList[0].subscriptionId
                    )
                } else subscriptionManager.activeSubscriptionInfoList[0].subscriptionId
            }

            val currentTime = System.currentTimeMillis()
            val transaction = Transaction(this, settings)

            val data = messages?.let {
                Message(
                    contactPhoneNumber = phoneNumber,
                    messageReadStatus = true,
                    messageType = 0,
                    messageTime = currentTime,
                    messageText = it
                )
            }
            val message = com.klinker.android.send_message.Message(
                messages,
                phoneNumber.filter { it != ' ' }
            )
            transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
            if (data != null) {
                updateConversationData(data)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateConversationData(message: Message) = job.launch {
        val messageId = messageRepository.insert(message).toInt()
        val conversation = messageRepository.getSingleConversation(message.contactPhoneNumber)
        val data = conversation?.conversation?.copy(
            messageId = messageId
        ) ?: Conversation(
            conversationLabel = 0,
            messageId = messageId,
            contactPhoneNumber = message.contactPhoneNumber,
            conversationProbability = arrayOf(1F, 0F, 0F)
        )
        if (conversation?.conversation != null) messageRepository.updateConversation(data)
        else messageRepository.insertConversation(data)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}