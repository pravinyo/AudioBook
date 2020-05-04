package com.allsoftdroid.audiobook.services.audio.utils

import com.allsoftdroid.common.base.extension.AudioPlayListItem
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalFilesForBookTest{

    private lateinit var localFilesForBook: LocalFilesForBook
    private val bookId = "random_book_unique_id"

    @Before
    fun setup(){
        localFilesForBook = mock(LocalFilesForBook::class.java)
    }

    @Test
    fun getListHavingOnlineAndOfflineUrl_noOfflineMedia_returnsOnlineURL(){
        val trackList = getTracks()

        `when`(localFilesForBook.getDownloadedFilesList(bookId)).thenReturn(null)
        `when`(localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)).thenCallRealMethod()

        val urls = localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)

        assertThat(urls.size, `is`(3))
    }

    @Test
    fun getListHavingOnlineAndOfflineUrl_noOfflineMedia_returnsOnlineURLCorrect(){

        val trackList = getTracks()
        `when`(localFilesForBook.getDownloadedFilesList(bookId)).thenReturn(null)
        `when`(localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)).thenCallRealMethod()

        val urls = localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)

        assertThat(urls[0].toString(), `is`("https://archive.org/download/random_book_unique_id/title1.mp3"))
    }

    @Test
    fun getListHavingOnlineAndOfflineUrl_presentOfflineMedia_returnsOnlineURLCorrect(){

        val trackList = getTracks()
        `when`(localFilesForBook.getDownloadedFilesList(bookId)).thenReturn(getOfflineTrackPath())
        `when`(localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)).thenCallRealMethod()

        val urls = localFilesForBook.getListHavingOnlineAndOfflineUrl(bookId,trackList)

        assertThat(urls[0].toString(), `is`("/local/path/title1.mp3"))
        assertThat(urls[1].toString(), `is`("https://archive.org/download/random_book_unique_id/title2.mp3"))
    }


    private fun parseItem(title:String,filename:String) = object:AudioPlayListItem {
        override val title: String?
            get() = title
        override val filename: String
            get() = filename
    }

    private fun getTracks():List<AudioPlayListItem>{
        val trackList = mutableListOf<AudioPlayListItem>()
        trackList.add(parseItem("title1","title1.mp3"))
        trackList.add(parseItem("title2","title2.mp3"))
        trackList.add(parseItem("title3","title3.mp3"))

        return trackList
    }

    private fun getOfflineTrackPath():List<String>{
        val trackList = mutableListOf<String>()
        trackList.add("/local/path/title1.mp3")

        return trackList
    }
}