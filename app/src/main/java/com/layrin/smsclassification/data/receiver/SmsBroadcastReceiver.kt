package com.layrin.smsclassification.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.layrin.smsclassification.data.service.SmsReceiverService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(
            Intent(context, SmsReceiverService::class.java).apply {
                action = intent?.action
                putExtra("pdus", intent?.extras?.get("pdus") as Array<*>)
                putExtra("format", intent.extras?.getString("format") as String)
            }
        )
    }

}