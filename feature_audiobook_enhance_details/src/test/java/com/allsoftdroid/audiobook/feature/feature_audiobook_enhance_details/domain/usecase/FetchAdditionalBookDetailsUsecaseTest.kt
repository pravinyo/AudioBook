package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

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

    @ExperimentalCoroutinesApi
    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        mainCoroutineRule.runBlockingTest {

            val useCaseHandler = UseCaseHandler.getInstance()
            val requestValues  = FetchAdditionalBookDetailsUsecase.RequestValues(bookUrl = BOOK_URL)

            useCaseHandler.execute(
                useCase = fetchAdditionalBookDetailsUsecase,
                values = requestValues,
                callback = object : BaseUseCase.UseCaseCallback<FetchAdditionalBookDetailsUsecase.ResponseValues> {
                    override suspend fun onSuccess(response: FetchAdditionalBookDetailsUsecase.ResponseValues) {
                        Timber.d("Result received : ${response.details}")
                        assertThat(response.details?.chapters?.size, `is`(0))
                    }

                    override suspend fun onError(t: Throwable) {
                        Timber.d("Enhanced Error:${t.message}")
                        assertThat(1, `is`(0))
                    }
                }
            )
        }
    }

}