package com.layrin.smsclassification.data.util

import android.database.Cursor

fun Cursor.getStringValue(key: String): String = getString(getColumnIndexOrThrow(key))

fun Cursor.getIntValue(key: String): Int = getInt(getColumnIndexOrThrow(key))