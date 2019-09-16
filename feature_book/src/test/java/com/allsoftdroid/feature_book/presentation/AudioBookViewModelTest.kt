package com.allsoftdroid.feature_book.presentation

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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

    lateinit var viewModel: AudioBookViewModel
    lateinit var albumUseCase: GetAudioBookListUsecase
    lateinit var application: Application
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        val repository : AudioBookRepository = FakeAudioBookRepository()
        albumUseCase = GetAudioBookListUsecase(repository)
        application = Application()

        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun viewModel_audiobooks_fetchSuccess(){
        viewModel = runBlocking(Dispatchers.Main) {
            AudioBookViewModel(getAlbumListUseCase = albumUseCase,application = application)
        }

        viewModel.audioBooks.observeForever {
            it?.let {
                Assert.assertSame("creator",it[0].creator)
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}
