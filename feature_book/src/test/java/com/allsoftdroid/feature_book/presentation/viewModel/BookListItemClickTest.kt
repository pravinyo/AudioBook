package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.feature_book.common.mock
import com.allsoftdroid.feature_book.di.bookListViewModelModule
import com.allsoftdroid.feature_book.di.jobModule
import com.allsoftdroid.feature_book.di.repositoryModule
import com.allsoftdroid.feature_book.di.usecaseModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.inject

class BookListItemClickTest : KoinTest {
    // Run tasks synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val application = mock<Application>()
    private val bookId ="bookId"
    private val viewModel: AudioBookListViewModel by inject{ parametersOf(application)}

    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        startKoin {
            modules(listOf(bookListViewModelModule,usecaseModule,repositoryModule, jobModule))
        }
    }

    @Test
    fun testBookListItem_ItemClick_SingleObserver(){

        runBlockingTest {
            viewModel.onBookItemClicked(bookId)

            val value = viewModel.itemClicked.getOrAwaitValue()

            Assert.assertThat(value.getContentIfNotHandled(),`is`(bookId))
        }
    }

    @Test
    fun testBookListItem_ItemClick_TwoObserver(){

        runBlockingTest {

            viewModel.onBookItemClicked(bookId)

            val value1 = viewModel.itemClicked.getOrAwaitValue()
            val value2 = viewModel.itemClicked.getOrAwaitValue()

            Assert.assertThat(value1.getContentIfNotHandled(),`is`(not(nullValue())))
            Assert.assertThat(value2.getContentIfNotHandled(),`is`((nullValue())))
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}