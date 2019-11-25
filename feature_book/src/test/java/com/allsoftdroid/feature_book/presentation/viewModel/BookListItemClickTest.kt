package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.common.FakeAudioBookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*

class BookListItemClickTest {
    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val repository = FakeAudioBookRepository()
    private val albumUsecase = GetAudioBookListUsecase(repository)
    private val application = Application()

    private val bookId ="bookId"
    private val viewModel by lazy {
        AudioBookListViewModel(application,albumUsecase)
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun testBookListItem_ItemClick_SingleObserver(){

        runBlocking {
            viewModel.onBookItemClicked(bookId)

            viewModel.itemClicked.observeForever {
                it.getContentIfNotHandled().let {
                    Assert.assertSame(bookId,it)
                }
            }
        }
    }

    @Test
    fun testBookListItem_ItemClick_TwoObserver(){
        runBlocking {

            viewModel.onBookItemClicked(bookId)

            viewModel.itemClicked.observeForever {
                it.getContentIfNotHandled().let {
                    Assert.assertSame(bookId,it)
                }
            }

            viewModel.itemClicked.observeForever {
                Assert.assertNull(it.getContentIfNotHandled())
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }
}