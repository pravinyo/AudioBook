package com.allsoftdroid.feature.book_details.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import com.allsoftdroid.feature.book_details.utils.FakeTrackListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

    @ExperimentalCoroutinesApi
    @Test
    fun trackListUsecase_EmptyList_returnsNothing(){
        mainCoroutineRule.runBlockingTest {
            try {
                trackListUsecase.getTrackListData().getOrAwaitValue()
            }catch (exception:TimeoutException){
                Assert.assertThat(exception.message, CoreMatchers.`is`("LiveData value was never set."))
            }
        }
    }

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
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