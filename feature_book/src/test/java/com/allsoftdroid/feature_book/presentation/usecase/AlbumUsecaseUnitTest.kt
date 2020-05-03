package com.allsoftdroid.feature_book.presentation.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.feature_book.data.repository.FakeAudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AlbumUsecaseUnitTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var audioBookRepository: AudioBookRepository
    private lateinit var albumUsecase :GetAudioBookListUsecase


    @ExperimentalCoroutinesApi
    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        mainCoroutineRule.runBlockingTest {

            audioBookRepository  = FakeAudioBookRepository(manualFailure = false)
            albumUsecase =   GetAudioBookListUsecase(audioBookRepository)

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))

            val list  = albumUsecase.getBookList().getOrAwaitValue()

            Assert.assertThat(list,`is`(notNullValue()))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testAudioBookListUsecase_requestFailed_returnsEmptyList(){
        mainCoroutineRule.runBlockingTest {
            audioBookRepository  = FakeAudioBookRepository(manualFailure = true)
            albumUsecase =   GetAudioBookListUsecase(audioBookRepository)

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))

            val list = albumUsecase.getBookList().getOrAwaitValue()

            Assert.assertThat(list.size,`is`(0))
        }
    }

}