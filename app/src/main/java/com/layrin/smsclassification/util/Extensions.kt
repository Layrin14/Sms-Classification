package com.layrin.smsclassification.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony.Sms

fun Context.saveMessage(number: String, message: String, type: Int): Int? {
    if (packageName != Sms.getDefaultSmsPackage(this)) return null
    val date = System.currentTimeMillis()
    val values = ContentValues().apply {
        put(Sms.ADDRESS, number)
        put(Sms.BODY, message)
        put(Sms.READ, 0)
        put(Sms.SEEN , 0)
        put(Sms.DATE, date)
        put(Sms.TYPE, type)
    }
    val uri = contentResolver.insert(Sms.CONTENT_URI, values)
    return uri?.lastPathSegment?.toInt()
}

fun Context.deleteMessage(id: Int) {
    if (packageName == Sms.getDefaultSmsPackage(this)) {
        contentResolver.delete(
            Uri.parse("content://sms/$id"),
            null,
            null
        )
    }
}

fun Context.updateMessageReadStatus(id: Int) {
    if (packageName == Sms.getDefaultSmsPackage(this)) {
        val values = ContentValues().apply {
            put(Sms.READ, 1)
            put(Sms.SEEN, 1)
        }
        contentResolver.update(
            Uri.parse("content://sms/$id"),
            values,
            null,
            null
        )
    }
}