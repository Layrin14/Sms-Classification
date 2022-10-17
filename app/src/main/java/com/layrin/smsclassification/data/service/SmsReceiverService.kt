package com.layrin.smsclassification.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import com.layrin.smsclassification.classifier.Model
import com.layrin.smsclassification.data.model.Conversation
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.data.repository.ContactRepository
import com.layrin.smsclassification.data.repository.ConversationRepository
import com.layrin.smsclassification.data.repository.MessageRepository
import com.layrin.smsclassification.util.saveMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiverService : Service() {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var conversationRepository: ConversationRepository

    @Inject
    lateinit var contactRepository: ContactRepository

    @Inject
    lateinit var model: Model

    private val scope = SupervisorJob()
    private val job = CoroutineScope(Dispatchers.IO + scope)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId)

        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            if (this.packageName != Telephony.Sms.getDefaultSmsPackage(this)) {
                job.launch {
                    conversationRepository.updateMessageData()
                }
            }
            return super.onStartCommand(intent, flags, startId)
        }

        val bundle = intent.extras
        if (bundle != null) {
            val pdu = bundle["pdus"] as Array<*>
            val senders = hashMapOf<String, String>()

            for (item in pdu) {
                val currentMessage =
                    SmsMessage.createFromPdu(item as ByteArray, bundle.getString("format"))
                val sender = currentMessage.displayOriginatingAddress
                val body = currentMessage.messageBody
                if (sender in senders) senders[sender] += body
                else senders[sender] = body
            }

            senders.forEach { (address, msgBody) ->
                val number = ContactProvider.getCleanNumber(address, this)
                job.launch {
                    val messageId = this@SmsReceiverService.saveMessage(
                        number,
                        msgBody,
                        1
                    ) ?: 0
                    val message = Message(
                        messageId = messageId,
                        contactPhoneNumber = number,
                        messageText = msgBody,
                        messageType = 1,
                        messageTime = System.currentTimeMillis()
                    )
                    val conversation = conversationRepository.getSingleConversation(number)
                    var probability: Array<Float>? = null
                    val prediction = if (contactRepository.getContactMap().containsKey(number)) 0
                    else {
                        probability = model.getPrediction(message)
                        if (conversation?.conversation != null)
                            for (i in 0..2) probability[i] += conversation.conversation.conversationProbability[i]
                        var label = probability.indexOf(probability.maxOrNull())
                        if (label == 0 && number.first().isLetter()) {
                            probability.clone().apply {
                                this[0] = 0f
                                label = probability.indexOf(probability.max())
                            }
                        }
                        label
                    }
                    val data = conversation?.conversation?.copy(
                        conversationProbability = probability
                            ?: arrayOf(0F),
                        conversationLabel = prediction,
                        messageId = messageId
                    ) ?: Conversation(
                        contactPhoneNumber = number,
                        conversationLabel = prediction,
                        conversationProbability = probability ?: arrayOf(0F),
                        messageId = messageId
                    )
                    if (conversation != null) {
                        conversationRepository.update(data)
                    } else conversationRepository.insert(data)
                    messageRepository.insert(message)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}