package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.audiobook.services.audio.AudioManager
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


class GetPlayingTrackProgressUsecaseTest{

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var audioManager: AudioManager
    private lateinit var playingTrackProgressUsecase: GetPlayingTrackProgressUsecase

    @Before
    fun setup(){
        audioManager = mock(AudioManager::class.java)
        playingTrackProgressUsecase = GetPlayingTrackProgressUsecase(audioManager)
    }

    @Test
    fun getProgress_returnsProgress(){

        runBlocking {
            `when`(audioManager.getPlayingTrackProgress()).thenReturn(0)

            playingTrackProgressUsecase.executeUseCase(null)

            playingTrackProgressUsecase.trackProgress.observeForever {
                assertThat(it,`is`(0))
            }
        }
    }
}