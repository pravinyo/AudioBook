package com.allsoftdroid.audiobook.services.audio.utils

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TextFormatterTest{
    @Test
    fun getPartialString_longString_returnsPartialString(){
        val longString = "Do what you love, not what you think you're supposed to do."
        val result = TextFormatter.getPartialString(longString)
        val expected = "Do what you love, no..."
        assertThat(result,`is`(expected))
    }

    @Test
    fun getPartialString_shortString_returnsSameString(){
        val longString = "Do what you love."
        val result = TextFormatter.getPartialString(longString)
        val expected = "Do what you love."
        assertThat(result,`is`(expected))
    }

    @Test
    fun getPartialString_LongEnoughString_returnsSameString(){
        val longString = "Do what you love, not_t"
        val result = TextFormatter.getPartialString(longString)
        val expected = "Do what you love, not_t"
        assertThat(result,`is`(expected))
    }

    @Test
    fun getPartialString_longHtmlString_returnsPartialString(){
        val longString = "<i>Do what you love</i>, not what you think you're supposed to do."
        val result = TextFormatter.getPartialString(longString)
        val expected = "Do what you love, no..."
        assertThat(result,`is`(expected))
    }
}