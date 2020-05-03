package com.allsoftdroid.feature_book.presentation.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.feature_book.data.repository.FakeAudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlbumUsecaseBookListUnitTest{

    // Run tasks synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var albumUsecase: GetAudioBookListUsecase
    private lateinit var useCaseHandler: UseCaseHandler
    private lateinit var requestValues : GetAudioBookListUsecase.RequestValues
    private lateinit var repository : AudioBookRepository


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        useCaseHandler = UseCaseHandler.getInstance()
        requestValues = GetAudioBookListUsecase.RequestValues(pageNumber = 1)
    }

    @Test
    fun albumUsecase_audioBooksFetchSuccess_returnsSuccessEvent(){
        mainCoroutineRule.runBlockingTest {
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
        mainCoroutineRule.runBlockingTest {
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
}
