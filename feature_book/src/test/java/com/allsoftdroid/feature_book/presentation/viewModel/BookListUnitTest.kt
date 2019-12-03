package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.common.FakeAudioBookRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*

class AudioBookViewModelTest{

    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

//    lateinit var viewModel: AudioBookListViewModel
    lateinit var albumUsecase: GetAudioBookListUsecase
    lateinit var application: Application
    lateinit var useCaseHandler: UseCaseHandler
    lateinit var requestValues : GetAudioBookListUsecase.RequestValues
    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")


    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        val repository : AudioBookRepository = FakeAudioBookRepository()
        albumUsecase = GetAudioBookListUsecase(repository)
        application = Application()
        useCaseHandler = UseCaseHandler.getInstance()
        requestValues = GetAudioBookListUsecase.RequestValues(pageNumber = 1)

        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun albumUsecase_audioBooks_fetchSuccess(){
        runBlocking {
            var event : Event<Any> = Event("NULL")

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

            event.getContentIfNotHandled().let {
                Assert.assertSame(Unit,it)
            }

        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}
