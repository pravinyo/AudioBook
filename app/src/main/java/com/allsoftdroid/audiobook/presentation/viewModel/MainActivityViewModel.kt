package com.allsoftdroid.audiobook.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainActivityViewModel(application : Application,
                            private val eventStore : AudioPlayerEventStore,
                            private val audioManager : AudioManager) : AndroidViewModel(application){

    private val _showMiniPlayer = MutableLiveData<Event<Boolean>>()
    val showPlayer :LiveData<Event<Boolean>> = _showMiniPlayer

    private var _stopService = MutableLiveData<Event<Boolean>>()
    val stopServiceEvent : LiveData<Event<Boolean>> = _stopService

    private var disposable : Disposable

    private var _playerEvent  = MutableLiveData<Event<AudioPlayerEvent>>()
    val playerEvent : LiveData<Event<AudioPlayerEvent>> = _playerEvent

    fun playerStatus( showPlayer : Boolean){
        _showMiniPlayer.value = Event(showPlayer)
    }

    init {
        _stopService.value = Event(false)

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _playerEvent.value = it
            }
    }

    fun playSelectedTrack(event: PlaySelectedTrack){
        audioManager.setPlayTrackList(event.trackList,event.bookId,event.bookName)
        audioManager.playTrackAtPosition(event.position)

        eventStore.publish(Event(
            TrackDetails(
                trackTitle = audioManager.getTrackTitle(),
                bookId = audioManager.getBookId(),
                position = event.position)
        ))
        Timber.d("Play selected track event")
    }

    fun nextTrack(event:Next){
        val state = event.result as PlayingState

        if(state.action_need) audioManager.playNext()

        eventStore.publish(Event(TrackDetails(
            trackTitle = audioManager.getTrackTitle(),
            bookId = audioManager.getBookId(),
            position = audioManager.currentPlayingIndex()+1)))

        Timber.d("Next event occurred")
    }

    fun previousTrack(event:Previous){
//        val state = event.result as PlayingState

        audioManager.playPrevious()

        eventStore.publish(Event(TrackDetails(
            trackTitle = audioManager.getTrackTitle(),
            bookId = audioManager.getBookId(),
            position = audioManager.currentPlayingIndex()+1)))
        Timber.d("Previous event occur")
    }

    fun pauseTrack(){
        audioManager.pauseTrack()
        Timber.d("pause event")
    }

    fun resumeOrPlayTrack(){
        audioManager.resumeTrack()
        Timber.d("Play/Resume track event")
    }

    fun bindAudioService(){
        audioManager.bindAudioService()
    }

    fun unBoundAudioService(){
        audioManager.unBoundAudioService()
    }


    override fun onCleared() {
        super.onCleared()
        _stopService.value = Event(true)
        disposable.dispose()
    }
}