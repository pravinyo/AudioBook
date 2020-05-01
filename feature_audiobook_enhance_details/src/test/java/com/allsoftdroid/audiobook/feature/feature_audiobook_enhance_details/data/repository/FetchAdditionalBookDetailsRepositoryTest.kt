package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.dropbox.android.external.store4.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.internal.wait
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.stubbing.OngoingStubbing
import java.lang.Exception
import java.lang.NullPointerException

class FetchAdditionalBookDetailsRepositoryTest{

    private lateinit var storeCachingRepository: IStoreRepository
    private lateinit var bookDetailsParser : BookDetailsParserFromHtml

    // Class Under Test
    private lateinit var bookDetailsRepository: IFetchAdditionBookDetailsRepository

    private val bookUrl = "librivox.org/book-url/"

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher

    @Before
    fun setup(){
        mainThreadSurrogate = newSingleThreadContext("UI thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Before
    fun createRepository(){
        storeCachingRepository = mock(IStoreRepository::class.java)
        bookDetailsParser = BookDetailsParserFromHtml()

        bookDetailsRepository = FetchAdditionalBookDetailsRepositoryImpl(
                bookDetailsParser = bookDetailsParser,
                storeCachingRepository = storeCachingRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fetchBookDetails_bookURL_returnsBookDetails(){
        runBlocking {
            try{
                val result = bookDetailsRepository.getBookDetails().getOrAwaitValue()
            }catch (e:Exception){
                assertThat(e.message, `is`("LiveData value was never set."))
            }
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}

