package com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.userAction.OpenMainPlayerUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import io.reactivex.disposables.Disposable
import timber.log.Timber

class MiniPlayerViewModel(
    private val eventStore : AudioPlayerEventStore,
    private val userActionEventStore: UserActionEventStore) : ViewModel(){

    private var _shouldItPlay:Boolean = true
    var shouldItPlay  = MutableLiveData<Boolean>()

    private var _trackTitle = MutableLiveData<String>()
    val trackTitle :LiveData<String> = _trackTitle

    private var _bookId = MutableLiveData<String>()
    val bookId :LiveData<String> = _bookId

    private var dispose : Disposable

    private var _isPlayerReady:Boolean= false

    private var _shouldWaitForPlayer = MutableLiveData<Boolean>()
    val shouldWaitForPlayer:LiveData<Boolean> = _shouldWaitForPlayer

    private var currentPlayingIndex = 0

    init {
        shouldItPlay.value = _shouldItPlay

        dispose = eventStore.observe()
            .subscribe {
                Timber.d("Peeking event default")
                it.peekContent().let {event ->
                    when(event){
                        is TrackDetails -> {
                            Timber.d("Received event for update track details event")
                            updateTrackDetails(title = event.trackTitle,bookId = event.bookId)
                            currentPlayingIndex = event.position
                        }

                        is PlaySelectedTrack -> {
                            setShouldPlay(play = true)
                        }

                        is Play -> {
                            setShouldPlay(play = true)
                        }

                        is Pause -> {
                            setShouldPlay(play = false)
                        }

                        is Next -> {
                            setShouldPlay(play = true)
                        }

                        is Previous -> {
                            setShouldPlay(play = true)
                        }

                        is AudioPlayerPlayingState -> setPlayerReady(event.isReady)

                        is Buffering -> setPlayerReady(isReady = false)
                    }
                }
            }
    }

    private fun setPlayerReady(isReady:Boolean){
        _isPlayerReady = isReady
        _shouldWaitForPlayer.value = !isReady
    }

    fun playPrevious(){
        Timber.d("Sending new Previous event")
        eventStore.publish(Event(
            Previous(
                PlayingState(
                    playingItemIndex = currentPlayingIndex - 1,
                    action_need = true
                )
            )
        ))
    }

    fun playNext(){
        Timber.d("Sending new next event")
        eventStore.publish(Event(
            Next(
                PlayingState(
                    playingItemIndex = currentPlayingIndex + 1,
                    action_need = true
                )
            )
        ))
    }

    fun playPause(){
        if(_isPlayerReady){
            _shouldItPlay = !_shouldItPlay
            shouldItPlay.value = _shouldItPlay

            if(_shouldItPlay){
                Timber.d("Sending new play event")
                eventStore.publish(Event(
                    Play(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                ))
            }else{
                Timber.d("Sending new pause event")
                eventStore.publish(Event(
                    Pause(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                ))
            }
        }else{
            Timber.d("Player is not ready")
        }
    }

    private fun setTrackTitle(title : String?){
        _trackTitle.value = title?:"UNKNOWN"
    }

    private fun setBookId(bookId:String){
        _bookId.value = bookId
    }
    
    private fun setShouldPlay(play:Boolean){
        _shouldItPlay = play
        shouldItPlay.value = _shouldItPlay
    }

    private fun updateTrackDetails(title:String,bookId:String) {
        setTrackTitle(title)
        setBookId(bookId)

        Timber.d("State change event sent: title : $title and book id:$bookId")
    }

    override fun onCleared() {
        super.onCleared()
        dispose.dispose()
    }
}