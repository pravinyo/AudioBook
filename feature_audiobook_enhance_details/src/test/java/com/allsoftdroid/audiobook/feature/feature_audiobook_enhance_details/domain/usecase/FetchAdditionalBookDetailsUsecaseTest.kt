package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FetchAdditionalBookDetailsUsecaseTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fetchAdditionBookDetailsRepository:IFetchAdditionBookDetailsRepository
    private lateinit var fetchAdditionalBookDetailsUsecase: FetchAdditionalBookDetailsUsecase

    private val BOOK_URL = "url"

    @Before
    fun createUsecase(){
        fetchAdditionBookDetailsRepository = FakeBookDetailsRepository()
        fetchAdditionalBookDetailsUsecase = FetchAdditionalBookDetailsUsecase(fetchAdditionBookDetailsRepository)
    }

    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        mainCoroutineRule.runBlockingTest {

            fetchAdditionalBookDetailsUsecase.executeUseCase(FetchAdditionalBookDetailsUsecase.RequestValues(BOOK_URL))

            val details = fetchAdditionalBookDetailsUsecase.getAdditionalBookDetails().getOrAwaitValue()

            assertThat(details.chapters.size, `is`(0))
        }
    }

}