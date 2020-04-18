package com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event

class MiniPlayerViewModel(application : Application) : AndroidViewModel(application){

    private var _previousTrackClicked = MutableLiveData<Event<Boolean>>()
    val previousTrackClicked : LiveData<Event<Boolean>> = _previousTrackClicked

    private var _nextTrackClicked = MutableLiveData<Event<Boolean>>()
    val nextTrackClicked : LiveData<Event<Boolean>> = _nextTrackClicked

    private var _playPauseClicked = MutableLiveData<Event<Boolean>>()
    val  playPausedClicked : LiveData<Event<Boolean>> = _playPauseClicked

    private var _shouldItPlay:Boolean = true
    var shouldItPlay  = MutableLiveData<Boolean>()

    private var _trackTitle = MutableLiveData<String>()
    val trackTitle :LiveData<String> = _trackTitle

    private var _bookId = MutableLiveData<String>()
    val bookId :LiveData<String> = _bookId

    private var _openMainPlayer = MutableLiveData<Event<Boolean>>()
    val openMainPlayerEvent :LiveData<Event<Boolean>> = _openMainPlayer

    init {
        shouldItPlay.value = _shouldItPlay
    }

    fun playPrevious(){
        _previousTrackClicked.value = Event(true)
    }

    fun playNext(){
        _nextTrackClicked.value = Event(true)
    }

    fun playPause(){
        _shouldItPlay = !_shouldItPlay
        _playPauseClicked.value = Event(_shouldItPlay)
        shouldItPlay.value = _shouldItPlay
    }

    fun setTrackTitle(title : String?){
        _trackTitle.value = title?:"UNKNOWN"
    }

    fun setBookId(bookId:String){
        _bookId.value = bookId
    }
    
    fun setShouldPlay(play:Boolean){
        _shouldItPlay = play
        shouldItPlay.value = _shouldItPlay
    }

    fun openMainPlayer(){
        _openMainPlayer.value = Event(true)
    }
}