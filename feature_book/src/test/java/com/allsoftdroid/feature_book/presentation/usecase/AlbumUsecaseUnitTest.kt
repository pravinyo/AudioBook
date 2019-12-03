package com.allsoftdroid.feature_book.presentation.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.common.mock
import com.allsoftdroid.feature_book.presentation.common.whenever
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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
    fun testAudioBookListUsecase_getBookList_Completed(){
        runBlocking {
            whenever(audioBookRepository.fetchBookList(0))
                .thenReturn(searchAudioBooks())
            whenever(audioBookRepository.getAudioBooks())
                .thenReturn(getAudioBooks())

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))
            albumUsecase.getBookList().let {
                Assert.assertNotNull(it)
            }
        }
    }

    @Test
    fun testAudioBookListUsecase_getBookList_response(){
        runBlocking {
            whenever(audioBookRepository.fetchBookList(0))
                .thenReturn(searchAudioBooks())
            whenever(audioBookRepository.getAudioBooks())
                .thenReturn(getAudioBooks())

            albumUsecase.executeUseCase(GetAudioBookListUsecase.RequestValues(0))
            albumUsecase.getBookList().observeForever {
                Assert.assertSame(list,it)
            }
        }
    }


    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }



    private fun searchAudioBooks() {
        list.add(AudioBookDomainModel("1","Title","creator","2019"))
        audioBooks.value = list
    }

    private fun getAudioBooks()= audioBooks

}