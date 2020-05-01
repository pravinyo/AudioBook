package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Assert
import org.junit.Test

class BookDetailsParserFromHtmlTest {

    private val systemUnderTest = BookDetailsParserFromHtml()

    @Test
    fun loadDetails_nonEmpty_returnsBookDetails() {
        //Arrange
        val page = "book_page.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertThat(details.chapters,not(emptyList()))
        Assert.assertThat(details.runtime.length,not(0))
    }

    @Test
    fun loadDetails_chapterCount_returnsBookDetails() {
        //Arrange
        val page = "book_page.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertThat(details.chapters.size,`is`(68))
    }

    @Test
    fun loadDetails_description_returnsBookDetails() {
        //Arrange
        val page = "book_page.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertThat(details.description.length,not(0))
    }

    @Test
    fun loadDetails_Empty_returnsBookDetails() {
        //Arrange
        val page = "book_page_error.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertEquals("",details.archiveUrl)
        Assert.assertEquals(0,details.chapters.size)
        Assert.assertEquals("",details.runtime)

        Assert.assertThat(details.chapters.size,`is`(0))
        Assert.assertThat(details.archiveUrl,`is`(""))
        Assert.assertThat(details.runtime,`is`(""))
    }

    private fun getHtmlResponse(path:String): String {
        val response = ClassLoader.getSystemResource(path)
        return response.readText(Charsets.UTF_8)
    }
}