package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.common.FakeAudioBookRepository
import com.allsoftdroid.feature_book.presentation.common.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*

class BookListUnitTest{

    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val repository = FakeAudioBookRepository()
    private val albumUsecase = GetAudioBookListUsecase(repository)
    private val application = Application()

    private val viewModel by lazy {
        AudioBookListViewModel(application,albumUsecase)
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun bookList_updateBookList_fetchSuccess(){

        runBlocking {
            viewModel.audioBooks.observeForever {
                Assert.assertSame(it,repository.getAudioBooks().value)
            }
        }
    }

    @Test
    fun testBookList_updateBookList_fetchFailure(){
        runBlocking {
            repository.setFailure()

            viewModel.errorResponse?.observeForever {
                it.getContentIfNotHandled()?.let {
                    Assert.assertSame((it as Throwable).message,Throwable("Error").message)
                }
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}
