package com.layrin.smsclassification.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.layrin.smsclassification.R
import java.util.*

class DateTimeUtil(
    private val context: Context,
) {

    fun getCondensedTime(time: Long): String {
        val messageTime = Calendar.getInstance().apply { timeInMillis = time }
        val now = Calendar.getInstance()

        return when {
            DateUtils.isToday(time) -> DateFormat.format(
                if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa", messageTime
            ).toString()
            DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS) -> context.getString(R.string.yesterday)
            now[Calendar.WEEK_OF_YEAR] == messageTime[Calendar.WEEK_OF_YEAR] &&
                    now[Calendar.YEAR] == messageTime[Calendar.YEAR] -> DateFormat.format(
                "EEEE", messageTime
            ).toString()
            now[Calendar.YEAR] == messageTime[Calendar.YEAR] -> DateFormat.format(
                "d MMMM", messageTime
            ).toString()
            else -> DateFormat.format("dd/MM/yyyy", messageTime).toString()
        }
    }

    fun getFullTime(time: Long): String {
        val messageTime = Calendar.getInstance().apply { timeInMillis = time }
        val now = Calendar.getInstance()

        val timeString = DateFormat.format(
            if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm aa",
            messageTime
        ).toString()

        return when {
            DateUtils.isToday(time) -> timeString
            DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS) -> "$timeString, \n${R.string.yesterday}"
            now[Calendar.YEAR] == messageTime[Calendar.YEAR] ->
                "$timeString, \n${DateFormat.format("d MMMM", messageTime)}"
            else -> DateFormat.format("dd/MM/yyyy", messageTime).toString()
        }
    }
}