package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils

import org.junit.Assert
import org.junit.Test
import java.io.File

class BestBookDetailsParserTest {

    private val systemUnderTest = BestBookDetailsParser()

    @Test
    fun getListOfWebDocuments_nonEmpty_returnsList() {
        //Arrange
        val htmlResponse:String = getHtmlResponse()
        //Act
        val result = systemUnderTest.getList(htmlResponse)
        //Assert
        Assert.assertNotEquals(0,result.size)
    }

    @Test
    fun getListWithRanks() {
        //Arrange
        //Act
        //Assert
    }


    private fun getHtmlResponse(): String {
        val response = ClassLoader.getSystemResource("response.html")
        return response.readText(Charsets.UTF_8)
    }
}