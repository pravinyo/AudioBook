package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils

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
        Assert.assertNotNull(details)
    }

    @Test
    fun loadDetails_chapterCount_returnsBookDetails() {
        //Arrange
        val page = "book_page.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertEquals(68,details.chapters.size)
    }

    @Test
    fun loadDetails_description_returnsBookDetails() {
        //Arrange
        val page = "book_page.html"
        val pageData = getHtmlResponse(path = page)
        //Act
        val details = systemUnderTest.loadDetails(pageData)
        //Assert
        Assert.assertNotEquals(0,details.description.length)
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
    }

    private fun getHtmlResponse(path:String): String {
        val response = ClassLoader.getSystemResource(path)
        return response.readText(Charsets.UTF_8)
    }
}