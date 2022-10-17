package com.layrin.smsclassification.classifier

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class PreprocessingTest {

    private lateinit var preprocessing: Preprocessing
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preprocessing = Preprocessing(context)
    }

    @Test
    fun whenCleaningText_shouldReturnLowercase() {
        val text = "EXPECTED"
        val result = preprocessing.cleanText(text)

        Assert.assertEquals(text.lowercase(), result)
    }

    @Test
    fun whenCleaningText_shouldReturnStringWithoutDuplicateSpace() {
        val text = "EXPECTED  "
        val expected = "expected"
        val result = preprocessing.cleanText(text)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun whenCleaningText_shouldReturnStringWithoutPunctuation() {
        val text = "This, sentence! have a lot? of - punctuation. \"just\" an 'ordinary': `sentence though"
        val expected = "this sentence have a lot of punctuation just an ordinary sentence though"
        val result = preprocessing.cleanText(text)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun whenCleaningText_shouldReturnStringWithoutBadSymbol() {
        val text = "this is my email: yes@email.com, send me *123# &^~$%^_=<>|"
        val expected = "this is my email yes email com send me 123"
        val result = preprocessing.cleanText(text)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun whenTokenize_shouldReturnTokenizedWord() {
        val text = "this is text"
        val expected = arrayOf("this", "is", "text")
        val result = preprocessing.tokenizer(text)

        Assert.assertArrayEquals(expected, result)
    }

    @Test
    fun whenTokenize_shouldReturnRemoveStopWords() {
        val text = "kalimat ini punya banyak stopwords di dalamnya"
        val expected = arrayOf("kalimat", "stopwords", "dalamnya")
        val result = preprocessing.tokenizer(text)

        Assert.assertArrayEquals(expected, result)
    }

    @Test
    fun whenConvertTextToSequence_ifWordsExistsInVocab_shouldReturnItsIndex() {
        val text = arrayOf("coba", "bang", "makan")
        val expected = intArrayOf(316, 1091, 628)
        val result = preprocessing.textToSequence(text)

        Assert.assertArrayEquals(expected, result)
    }

    @Test
    fun whenConvertTextToSequence_ifWordsNotExistsInVocab_shouldReturnZero() {
        val text = arrayOf("this", "is", "word")
        val expected = intArrayOf(0, 0, 0)
        val result = preprocessing.textToSequence(text)

        Assert.assertArrayEquals(expected, result)
    }

    @Test
    fun whenPadSequence_ifTheSequenceLessThanThirteen_shouldReturnArrayWithSizeThirteen() {
        val data = generateRandomSequence(5)
        val result = preprocessing.padSequence(data)

        Assert.assertEquals(13, result.size)
    }

    @Test
    fun whenPadSequence_ifTheSequenceLessThanThirteen_shouldReturnArrayWithZeroPadded() {
        val data = generateRandomSequence(7)
        val result = preprocessing.padSequence(data)

        Assert.assertEquals(0, result.last())
    }

    @Test
    fun whenPadSequence_ifTheSequenceMoreThanThirteen_shouldReturnArrayWithSizeThirteen() {
        val data = generateRandomSequence(20)
        val result = preprocessing.padSequence(data)

        Assert.assertEquals(13, result.size)
    }

    private fun generateRandomSequence(size: Int): IntArray {
        val random = Random(10)
        val data = arrayListOf<Int>()
        for (i in 0 until size) data.add(random.nextInt(1, 100))
        return data.toIntArray()
    }
}