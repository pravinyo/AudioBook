package com.allsoftdroid.feature.book_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.store.downloader.Download
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsEqual
import org.junit.*


class GetDownloadUsecaseTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var eventStore: DownloadEventStore
    private lateinit var downloadUsecase: GetDownloadUsecase


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){

        eventStore = DownloaderEventBus.getEventBusInstance()
        downloadUsecase = GetDownloadUsecase(eventStore)

        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun downloadUsecase_eventPublished_returnsNothing(){
        runBlocking {

            val download = Download(
                url = "url",
                name = "book",
                description = "book download",
                subPath = "/downloads",
                bookId = "bookId",
                chapter = "Intro",
                chapterIndex = 0
            )

            downloadUsecase.executeUseCase(GetDownloadUsecase.RequestValues(download))

            val result  = eventStore.observe().subscribe {
                Assert.assertThat(it.getContentIfNotHandled(), CoreMatchers.`is`(CoreMatchers.notNullValue()))
            }

            result.dispose()
        }
    }

    @Test
    fun downloadUsecase_eventReceived_returnsNothing(){
        runBlockingTest {

            val download = Download(
                url = "url",
                name = "book",
                description = "book download",
                subPath = "/downloads",
                bookId = "bookId",
                chapter = "Intro",
                chapterIndex = 0
            )

            downloadUsecase.executeUseCase(GetDownloadUsecase.RequestValues(download))

            val result  =
            eventStore.observe().subscribe{ it ->
                it.getContentIfNotHandled()!!.let {downloadEvent->
                    Assert.assertThat(downloadEvent.bookId,IsEqual("bookId"))
                }
            }

            result.dispose()
        }
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }
}