package com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.userAction.OpenMainPlayerUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventBus
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.test.getOrAwaitValue
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MiniPlayerViewModelTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var playerViewModel:MiniPlayerViewModel
    private lateinit var eventStore : AudioPlayerEventStore
    private lateinit var userActionEventStore: UserActionEventStore

    @Before
    fun setup(){
        eventStore = AudioPlayerEventBus.getEventBusInstance()
        userActionEventStore = UserActionEventBus.getEventBusInstance()
        playerViewModel = MiniPlayerViewModel(eventStore,userActionEventStore)
    }

    @Test
    fun shouldItPlay_initialState_returnsTrue(){
        val shouldPlay = playerViewModel.shouldItPlay.getOrAwaitValue()
        assertThat(shouldPlay,`is`(true))
    }

    @Test
    fun playPause_playerReady_eventSent(){

        eventStore.publish(
            Event(AudioPlayerPlayingState(isReady = true))
        )

        playerViewModel.playPause()

        playerViewModel.shouldItPlay.observeForever {
            assertThat(it,`is`(false))
        }
    }

    @Test
    fun playPause_playerNotReady_eventNotSent(){

        playerViewModel.playPause()

        playerViewModel.shouldItPlay.observeForever {
            assertThat(it,`is`(true))
        }
    }

    @Test
    fun playPrevious_playerReady_eventSent(){

        playerViewModel.playPrevious()

        playerViewModel.shouldItPlay.observeForever {
            assertThat(it,`is`(true))
        }

        eventStore.observe().subscribe {
            it.getContentIfNotHandled()?.let {event->

                val eventType = when(event){
                    is Previous -> "Previous"
                    else -> "Not Previous"
                }
                assertThat(eventType, `is`("Previous"))
            }
        }
    }

    @Test
    fun playNext_playerReady_eventSent(){

        playerViewModel.playNext()

        playerViewModel.shouldItPlay.observeForever {
            assertThat(it,`is`(true))
        }

        eventStore.observe().subscribe {
            it.getContentIfNotHandled()?.let {event->

                val eventType = when(event){
                    is Next -> "Next"
                    else -> "Not Next"
                }
                assertThat(eventType, `is`("Next"))
            }
        }
    }
}