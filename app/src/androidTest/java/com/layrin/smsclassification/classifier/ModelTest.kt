package com.layrin.smsclassification.classifier

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.layrin.smsclassification.data.model.Message
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@SmallTest
class ModelTest {

    private lateinit var model: Model
    private lateinit var context: Context
    private lateinit var preprocessing: Preprocessing

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preprocessing = Preprocessing(context)
        model = Model(context, preprocessing)
    }

    @After
    fun tearDown() {
        model.close()
    }

    @Test
    fun whenGetMessagePrediction_shouldReturnFloatProbability() {
        val data = Message(
            messageId = 0,
            contactPhoneNumber = "0",
            messageText = "Hello",
            messageType = 0,
            messageTime = 1L,
            messageSentStatus = true
        )
        val actual = model.getPrediction(data)
        model.close()
        val expected = 0

        assertEquals(expected, actual.indexOf(actual.max()))
    }

    @Test
    fun whenGetMultipleMessagePredictions_shouldReturnHighestProbability() {
        val data1 = Message(
            messageId = 0,
            contactPhoneNumber = "0",
            messageText = "hai, apa kabar?",
            messageType = 0,
            messageTime = 1L,
            messageSentStatus = true
        )
        val data2 = Message(
            messageId = 1,
            contactPhoneNumber = "0",
            messageText = "baik, km gimana?",
            messageType = 0,
            messageTime = 1L,
            messageSentStatus = true
        )
        val data3 = Message(
            messageId = 0,
            contactPhoneNumber = "0",
            messageText = "biasa aja si",
            messageType = 0,
            messageTime = 1L,
            messageSentStatus = true
        )
        val data = arrayListOf(data1, data2, data3)
        val actual = model.getPredictions(data)
        val expected = 0

        assertEquals(expected, actual.indexOf(actual.max()))
    }
}