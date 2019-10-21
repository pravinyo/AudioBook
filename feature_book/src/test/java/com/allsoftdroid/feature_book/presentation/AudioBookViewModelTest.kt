package com.allsoftdroid.feature_book.presentation

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
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
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")


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
                        event = response.event
                    }

                    override suspend fun onError(t: Throwable) {
                        event = Event("NULL")
                    }
                }
            )

            event.getContentIfNotHandled().let {
                Assert.assertSame(Unit,it)
            }

        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}
