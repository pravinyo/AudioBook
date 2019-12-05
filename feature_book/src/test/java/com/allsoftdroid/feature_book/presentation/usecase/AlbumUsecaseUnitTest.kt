package com.allsoftdroid.feature_book.presentation.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.common.mock
import com.allsoftdroid.feature_book.presentation.common.whenever
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

    private val audioBookRepository  = mock<AudioBookRepository>()
    private val albumUsecase by lazy{
        GetAudioBookListUsecase(audioBookRepository)
    }
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()
    private val list = ArrayList<AudioBookDomainModel>()


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun testAudioBookListUsecase_requestCompleted_returnsList(){
        runBlocking {
            whenever(audioBookRepository.fetchBookList(0))
                .thenReturn(searchAudioBooks(isError = false))
            whenever(audioBookRepository.getAudioBooks())
                .thenReturn(getAudioBooks())

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))
            albumUsecase.getBookList().let {
                Assert.assertThat(it.value,`is`(notNullValue()))
            }
        }
    }

    @Test
    fun testAudioBookListUsecase_requestFailed_returnsEmptyList(){
        runBlocking {
            whenever(audioBookRepository.fetchBookList(0))
                .thenReturn(searchAudioBooks(isError = true))
            whenever(audioBookRepository.getAudioBooks())
                .thenReturn(getAudioBooks())

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))
            albumUsecase.getBookList().let {
                Assert.assertThat(it.value?.size,`is`(0))
            }
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }



    private fun searchAudioBooks(isError:Boolean) {
        if(!isError){
            list.add(AudioBookDomainModel("1","Title","creator","2019"))
            audioBooks.value = list
        }else{
            audioBooks.value = emptyList()
        }
    }

    private fun getAudioBooks()= audioBooks

}