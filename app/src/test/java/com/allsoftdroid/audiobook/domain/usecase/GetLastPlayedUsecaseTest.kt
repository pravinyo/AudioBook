package com.allsoftdroid.audiobook.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.utils.FakeBookDetailsSharedPref
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.test.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetLastPlayedUsecaseTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeBookDetailsSharedPref: FakeBookDetailsSharedPref
    private lateinit var lastPlayedUsecase: GetLastPlayedUsecase
    private lateinit var handler: UseCaseHandler

    private val trackPos = 0
    private val trackTitle = "First"
    private val bookId = "bookId"
    private val bookname = "Fake Book"
    private val trackFormat = 0
    private val isPlaying = false

    @Before
    fun setup(){
        fakeBookDetailsSharedPref = FakeBookDetailsSharedPref()
        lastPlayedUsecase  = GetLastPlayedUsecase()
        handler = UseCaseHandler.getInstance()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun lastPlayed_returnsData() = runBlockingTest {

        fakeBookDetailsSharedPref.saveBookId(bookId)
        fakeBookDetailsSharedPref.saveBookName(bookname)

        val request = GetLastPlayedUsecase.RequestValues(sharedPref = fakeBookDetailsSharedPref)

        handler.execute(lastPlayedUsecase,request,object : BaseUseCase.UseCaseCallback<GetLastPlayedUsecase.ResponseValues>{
            override suspend fun onSuccess(response: GetLastPlayedUsecase.ResponseValues) {
                assertThat(response.lastPlayedTrack,`is`(notNullValue()))
                assertThat(response.lastPlayedTrack!!.bookId,`is`(bookId))
            }

            override suspend fun onError(t: Throwable) {
                assertThat(1,`is`(2))
            }
        })
    }

    @ExperimentalCoroutinesApi
    @Test
    fun lastPlayed_returnsNull() = runBlockingTest {
        fakeBookDetailsSharedPref.clear()
        val request = GetLastPlayedUsecase.RequestValues(sharedPref = fakeBookDetailsSharedPref)

        handler.execute(lastPlayedUsecase,request,object : BaseUseCase.UseCaseCallback<GetLastPlayedUsecase.ResponseValues>{
            override suspend fun onSuccess(response: GetLastPlayedUsecase.ResponseValues) {
                assertThat(response.lastPlayedTrack,`is`(nullValue()))
            }

            override suspend fun onError(t: Throwable) {
                assertThat(1,`is`(2))
            }
        })
    }
}