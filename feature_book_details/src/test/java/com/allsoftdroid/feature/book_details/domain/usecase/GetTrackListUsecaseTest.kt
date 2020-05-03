package com.allsoftdroid.feature.book_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import com.allsoftdroid.feature.book_details.utils.FakeTrackListRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.*
import java.util.concurrent.TimeoutException


class GetTrackListUsecaseTest{
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var trackListRepository: ITrackListRepository
    private lateinit var trackListUsecase: GetTrackListUsecase


    @ExperimentalCoroutinesApi
    @Before
    fun setup(){

        trackListRepository =
            FakeTrackListRepository()
        trackListUsecase = GetTrackListUsecase(trackListRepository)
    }

    @Test
    fun trackListUsecase_EmptyList_returnsNothing(){
        mainCoroutineRule.runBlockingTest {
            try {
                val list = trackListUsecase.getTrackListData().getOrAwaitValue()
            }catch (exception:TimeoutException){
                Assert.assertThat(exception.message, CoreMatchers.`is`("LiveData value was never set."))
            }
        }
    }

    @Test
    fun trackListUsecase_PullTracks_returnsNonNullTrackList(){
        mainCoroutineRule.runBlockingTest {

            trackListUsecase.executeUseCase(GetTrackListUsecase.RequestValues(TrackFormat.FormatBP64))

            val list = trackListUsecase.getTrackListData().getOrAwaitValue()

            Assert.assertThat(list,`is`(notNullValue()))
            Assert.assertThat(list.size,`is`(2))
            Assert.assertThat(list[1].trackId,`is`("2"))
        }
    }

    @Test
    fun trackListUsecase_PullTracks_returnsTrackList(){
        mainCoroutineRule.runBlockingTest {
            try {
                trackListUsecase.executeUseCase(null)
                trackListUsecase.getTrackListData().getOrAwaitValue()
            }catch (exception:TimeoutException){
                Assert.assertThat(exception.message, CoreMatchers.`is`("LiveData value was never set."))
            }
        }
    }
}