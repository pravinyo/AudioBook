package com.allsoftdroid.feature.book_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.store.downloader.Download
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import kotlinx.coroutines.*
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


    private lateinit var eventStore: DownloadEventStore
    private lateinit var downloadUsecase: GetDownloadUsecase
    private lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){
        mainThreadSurrogate = newSingleThreadContext("UI thread")

        eventStore = DownloaderEventBus.getEventBusInstance()
        downloadUsecase = GetDownloadUsecase(eventStore)

        Dispatchers.setMain(mainThreadSurrogate)
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
        mainThreadSurrogate.close()
    }
}