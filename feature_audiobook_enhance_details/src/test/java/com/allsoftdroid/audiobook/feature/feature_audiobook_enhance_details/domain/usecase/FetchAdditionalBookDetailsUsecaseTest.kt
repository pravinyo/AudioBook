package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.getOrAwaitValue
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.Assert.assertThat
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class FetchAdditionalBookDetailsUsecaseTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher
    private lateinit var fetchAdditionBookDetailsRepository:IFetchAdditionBookDetailsRepository
    private lateinit var fetchAdditionalBookDetailsUsecase: FetchAdditionalBookDetailsUsecase

    private val BOOK_URL = "url"

    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        mainThreadSurrogate = newSingleThreadContext("UI thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Before
    fun createUsecase(){
        fetchAdditionBookDetailsRepository = FakeBookDetailsRepository()
        fetchAdditionalBookDetailsUsecase = FetchAdditionalBookDetailsUsecase(fetchAdditionBookDetailsRepository)
    }

    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        runBlocking {

            fetchAdditionalBookDetailsUsecase.executeUseCase(FetchAdditionalBookDetailsUsecase.RequestValues(BOOK_URL))

            val details = fetchAdditionalBookDetailsUsecase.getAdditionalBookDetails().getOrAwaitValue()

            assertThat(details.chapters.size, `is`(0))
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

}