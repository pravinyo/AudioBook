package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayerControlState
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.audioPlayer.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class MainPlayerViewModel(private val eventStore : AudioPlayerEventStore) : ViewModel(), KoinComponent {

    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var _playerControlState = MutableLiveData<Event<PlayerControlState>>()
    val previousControlState : LiveData<Event<PlayerControlState>> = _playerControlState

    private var _shouldItPlay:Boolean = true
    var shouldItPlay  = MutableLiveData<Boolean>()

    private var _playingTrackDetails = MutableLiveData<PlayingTrackDetails>()
    val playingTrackDetails:LiveData<PlayingTrackDetails> = _playingTrackDetails

    private var currentPlayingIndex = 0

    fun playPrevious(){
        _playerControlState.value = Event(PlayerControlState(playPrevious = true))

        Timber.d("Sending new Previous event")
        eventStore.publish(
            Event(
                Previous(
                    PlayingState(
                        playingItemIndex = currentPlayingIndex - 1,
                        action_need = true
                    )
                )
            )
        )
    }

    fun playNext(){
        _playerControlState.value = Event(PlayerControlState(playNext = true))

        Timber.d("Sending new next event")
        eventStore.publish(
            Event(
                Next(
                    PlayingState(
                        playingItemIndex = currentPlayingIndex + 1,
                        action_need = true
                    )
                )
            )
        )
    }

    fun playPause(){
        _playerControlState.value = Event(PlayerControlState(shouldItPlay = _shouldItPlay))
        setShouldPlay(!_shouldItPlay)
        shouldPlayEvent()
    }

    fun setBookDetails(bookId:String, bookName:String, trackName:String, currentPlayingTrack: Int, totalChapter:Int){

        _playingTrackDetails.value = PlayingTrackDetails(
            bookIdentifier = bookId,
            bookTitle = bookName,
            name = trackName,
            chapterIndex = currentPlayingTrack,
            totalChapter = totalChapter
        )

        currentPlayingIndex = currentPlayingTrack
    }

    fun setShouldPlay(play:Boolean){
        _shouldItPlay = play
        shouldItPlay.value = _shouldItPlay
    }

    private fun shouldPlayEvent(){
        Timber.d("should play event is $_shouldItPlay")

        if(_shouldItPlay){
            Timber.d("Sending new play event")
            eventStore.publish(
                Event(
                    Play(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                )
            )
        }else{
            Timber.d("Sending new pause event")
            eventStore.publish(
                Event(
                    Pause(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                )
            )
        }
    }

    fun showMiniPlayer(){
        eventStore.publish(Event(OpenMiniPlayerEvent))
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}