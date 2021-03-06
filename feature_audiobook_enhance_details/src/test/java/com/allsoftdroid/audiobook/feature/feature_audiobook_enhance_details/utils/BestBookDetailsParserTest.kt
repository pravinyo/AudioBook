package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Assert
import org.junit.Test

class BestBookDetailsParserTest {

    private val systemUnderTest = BestBookDetailsParser()
    private val responsePath = "response.html"
    private val noResponse = "noResult.html"

    @Test
    fun getListOfWebDocuments_nonEmpty_returnsList() {
        //Arrange
        val htmlResponse:String = getResponse(isFound = true)
        //Act
        val result = systemUnderTest.getList(htmlResponse)
        //Assert
        Assert.assertThat(result.size, not(0))
    }

    @Test
    fun getListOfWebDocuments_Empty_returnsList() {
        //Arrange
        val htmlResponse:String = getResponse(isFound = false)
        //Act
        val result = systemUnderTest.getList(htmlResponse)
        //Assert
        Assert.assertThat(result.size,`is`(0))
    }

    @Test
    fun getListWithRanks_returnHighRankItem() {
        //Arrange
        val htmlResponse:String = getResponse(isFound = true)
        //Act
        val result = systemUnderTest.getList(htmlResponse)
        val ranks = systemUnderTest.getListWithRanks(result,"poem","Frank Oliver CALL (1878 - 1956)")
        //Assert
        Assert.assertThat(ranks.size, not(0))
    }

    @Test
    fun getListWithRanks_returnEmpty() {
        //Arrange
        val htmlResponse:String = getResponse(isFound = false)
        //Act
        val result = systemUnderTest.getList(htmlResponse)
        val ranks = systemUnderTest.getListWithRanks(result,"poem","Frank Oliver CALL (1878 - 1956)")
        //Assert
        Assert.assertThat(ranks.size, `is`(0))
    }

    private fun getResponse(isFound:Boolean):String{
        return if (isFound) getHtmlResponse(responsePath) else getHtmlResponse(noResponse)
    }

    private fun getHtmlResponse(path:String): String {
        val response = ClassLoader.getSystemResource(path)
        return response.readText(Charsets.UTF_8)
    }
}