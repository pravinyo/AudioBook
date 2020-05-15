package com.allsoftdroid.feature.book_details.presentation.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.FetchAdditionalBookDetailsUsecase
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.SearchBookDetailsUsecase
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import com.allsoftdroid.common.base.store.userAction.UserActionEventBus
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetDownloadUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.ListenLaterUsecase
import com.allsoftdroid.feature.book_details.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class BookDetailsViewModelTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var bookDetailsViewModel: BookDetailsViewModel
    private lateinit var sharedPref: BookDetailsSharedPreferenceRepository
    private lateinit var metadataUsecase: GetMetadataUsecase
    private lateinit var useCaseHandler: UseCaseHandler
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var downloadUsecase: GetDownloadUsecase
    private lateinit var searchBookDetailsUsecase: SearchBookDetailsUsecase
    private lateinit var fetchAdditionBookDetailsUsecase: FetchAdditionalBookDetailsUsecase
    private lateinit var trackListUsecase: GetTrackListUsecase
    private lateinit var listenLaterUsecase: ListenLaterUsecase
    private val bookId = "bookId"

    @Before
    fun setup(){
        sharedPref = FakeBookDetailsSharedPref()
        savedStateHandle = SavedStateHandle()
        useCaseHandler = UseCaseHandler.getInstance()
        metadataUsecase = GetMetadataUsecase(FakeMetadataRepository(bookId = bookId))
        downloadUsecase = GetDownloadUsecase(DownloaderEventBus.getEventBusInstance())
        searchBookDetailsUsecase = SearchBookDetailsUsecase(FakeSearchBookDetailsRepository())
        fetchAdditionBookDetailsUsecase = FetchAdditionalBookDetailsUsecase(FakeFetchAdditionBookDetailsRepository())
        trackListUsecase = GetTrackListUsecase(FakeTrackListRepository())
        listenLaterUsecase = ListenLaterUsecase(FakeListenLaterRepository())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun bookDetails_loading(){

        mainCoroutineRule.pauseDispatcher()

        bookDetailsViewModel = BookDetailsViewModel(
            sharedPref = sharedPref,
            stateHandle = savedStateHandle,
            useCaseHandler = useCaseHandler,
            getMetadataUsecase = metadataUsecase,
            downloadUsecase = downloadUsecase,
            searchBookDetailsUsecase = searchBookDetailsUsecase,
            getFetchAdditionalBookDetailsUseCase = fetchAdditionBookDetailsUsecase,
            getTrackListUsecase = trackListUsecase,
            listenLaterUsecase = listenLaterUsecase,
            userActionEventStore = UserActionEventBus.getEventBusInstance()
        )

        assertThat(bookDetailsViewModel.networkResponse.getOrAwaitValue().peekContent(),`is`(NetworkState.LOADING))

        mainCoroutineRule.resumeDispatcher()
        assertThat(bookDetailsViewModel.networkResponse.getOrAwaitValue().peekContent(),`is`(NetworkState.COMPLETED))
    }

}