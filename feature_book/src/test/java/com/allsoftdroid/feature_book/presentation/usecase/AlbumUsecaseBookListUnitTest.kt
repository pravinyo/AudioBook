package com.allsoftdroid.feature_book.presentation.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.data.repository.FakeAudioBookRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*

class AlbumUsecaseBookListUnitTest{

    // Run tasks synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var albumUsecase: GetAudioBookListUsecase
    private lateinit var application: Application
    private lateinit var useCaseHandler: UseCaseHandler
    private lateinit var requestValues : GetAudioBookListUsecase.RequestValues
    private lateinit var repository : AudioBookRepository


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        application = Application()
        useCaseHandler = UseCaseHandler.getInstance()
        requestValues = GetAudioBookListUsecase.RequestValues(pageNumber = 1)

        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun albumUsecase_audioBooksFetchSuccess_returnsSuccessEvent(){
        runBlocking {
            var event : Event<Any> = Event("NULL")

            repository= FakeAudioBookRepository(
                manualFailure = false
            )
            albumUsecase = GetAudioBookListUsecase(repository)

            useCaseHandler.execute(
                useCase = albumUsecase,
                values = requestValues,
                callback = object : BaseUseCase.UseCaseCallback<GetAudioBookListUsecase.ResponseValues>{
                    override suspend fun onSuccess(response: GetAudioBookListUsecase.ResponseValues) {
                        event = Event(Unit)
                    }

                    override suspend fun onError(t: Throwable) {
                        event = Event(t.message?:"ERROR")
                    }
                }
            )

            Assert.assertSame(event.getContentIfNotHandled(),Unit)

        }
    }

    @Test
    fun albumUsecase_audioBooksFetchFailure_returnsFailureEvent(){
        runBlocking {
            var event : Event<Any> = Event("NULL")

            repository= FakeAudioBookRepository(
                manualFailure = true
            )
            albumUsecase = GetAudioBookListUsecase(repository)

            useCaseHandler.execute(
                useCase = albumUsecase,
                values = requestValues,
                callback = object : BaseUseCase.UseCaseCallback<GetAudioBookListUsecase.ResponseValues>{
                    override suspend fun onSuccess(response: GetAudioBookListUsecase.ResponseValues) {
                        event = Event(Unit)
                    }

                    override suspend fun onError(t: Throwable) {
                        event = Event(t.message?:"")
                    }
                }
            )

            Assert.assertSame(FakeAudioBookRepository.FAILURE_MESSAGE,event.getContentIfNotHandled())
        }
    }


    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }
}
