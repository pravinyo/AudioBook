package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.feature_book.presentation.common.mock
import com.allsoftdroid.feature_book.presentation.di.bookListViewModelModule
import com.allsoftdroid.feature_book.presentation.di.jobModule
import com.allsoftdroid.feature_book.presentation.di.repositoryModule
import com.allsoftdroid.feature_book.presentation.di.usecaseModule
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito

class BookListItemClickTest : KoinTest {
    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val application = mock<Application>()

    private val bookId ="bookId"
    private val viewModel: AudioBookListViewModel by inject{ parametersOf(application)}


    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        Dispatchers.setMain(mainThreadSurrogate)
        startKoin {
            modules(listOf(bookListViewModelModule,usecaseModule,repositoryModule, jobModule))
        }
    }

    @Test
    fun testBookListItem_ItemClick_SingleObserver(){

        runBlocking {
            viewModel.onBookItemClicked(bookId)

            viewModel.itemClicked.observeForever {
                it.getContentIfNotHandled().let {result->
                    Assert.assertSame(bookId,result)
                }
            }
        }
    }

    @Test
    fun testBookListItem_ItemClick_TwoObserver(){

        runBlocking {

            viewModel.onBookItemClicked(bookId)

            viewModel.itemClicked.observeForever {
                it.getContentIfNotHandled().let {result ->
                    Assert.assertSame(bookId,result)
                }
            }

            viewModel.itemClicked.observeForever {
                Assert.assertNull(it.getContentIfNotHandled())
            }
        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
        stopKoin()
    }
}