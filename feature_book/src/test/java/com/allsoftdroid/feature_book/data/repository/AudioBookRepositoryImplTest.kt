package com.allsoftdroid.feature_book.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.feature_book.data.databaseExtension.SaveBookListInDatabase
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.common.getOrAwaitValue
import com.allsoftdroid.feature_book.common.mock
import com.allsoftdroid.feature_book.common.whenever
import com.allsoftdroid.feature_book.data.dataSource.FakeAudioDataSource
import com.allsoftdroid.feature_book.data.dataSource.FakeRemoteBookService
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.junit.*

class AudioBookRepositoryImplTest{

    private lateinit var audioBookDao: FakeAudioDataSource
    private lateinit var remoteBookService: FakeRemoteBookService
    private lateinit var saveInDatabase: SaveInDatabase<AudioBookDao,SaveBookListInDatabase>
    private lateinit var audioBookRepository: AudioBookRepository
    private lateinit var list : ArrayList<AudioBookDomainModel>


    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        audioBookDao = FakeAudioDataSource()
        remoteBookService = mock()
        saveInDatabase = SaveBookListInDatabase(audioBookDao)

        audioBookRepository = AudioBookRepositoryImpl(
            bookDao = audioBookDao,
            remoteBookService = remoteBookService,
            saveInDatabase = saveInDatabase
        )

        list = ArrayList()

        Dispatchers.setMain(mainThreadSurrogate)
    }



    @Test
    fun fetchBookList(){

        runBlocking {

            whenever(audioBookRepository.fetchBookList(0))
                .thenReturn(searchAudioBooks())
            audioBookRepository.fetchBookList(0)

            val list = audioBookRepository.getAudioBooks().getOrAwaitValue()
            Assert.assertThat(list,`is`(not(nullValue())))
        }
    }

    private fun searchAudioBooks(isError:Boolean = false) {

        val data = DatabaseAudioBook("1","Title","creator","2019")
        audioBookDao.insert(data)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}