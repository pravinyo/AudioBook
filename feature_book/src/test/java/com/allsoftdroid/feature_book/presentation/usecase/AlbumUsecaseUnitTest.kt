package com.allsoftdroid.feature_book.presentation.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.common.getOrAwaitValue
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.common.mock
import com.allsoftdroid.feature_book.common.whenever
import com.allsoftdroid.feature_book.data.repository.FakeAudioBookRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*


@ObsoleteCoroutinesApi
class AlbumUsecaseUnitTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()


    private lateinit var audioBookRepository: AudioBookRepository
    private lateinit var albumUsecase :GetAudioBookListUsecase
    private lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){

        mainThreadSurrogate = newSingleThreadContext("UI thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        runBlocking {

            audioBookRepository  = FakeAudioBookRepository(manualFailure = false)
            albumUsecase =   GetAudioBookListUsecase(audioBookRepository)

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))

            val list  = albumUsecase.getBookList().getOrAwaitValue()

            Assert.assertThat(list,`is`(notNullValue()))
        }
    }

    @Test
    fun testAudioBookListUsecase_requestFailed_returnsEmptyList(){
        runBlocking {
            audioBookRepository  = FakeAudioBookRepository(manualFailure = true)
            albumUsecase =   GetAudioBookListUsecase(audioBookRepository)

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))

            val list = albumUsecase.getBookList().getOrAwaitValue()

            Assert.assertThat(list.size,`is`(0))
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

}