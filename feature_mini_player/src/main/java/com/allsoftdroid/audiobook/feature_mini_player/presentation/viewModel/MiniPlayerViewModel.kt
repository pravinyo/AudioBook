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
    var  playPausedClicked : LiveData<Event<Boolean>> = _playPauseClicked

    private var shouldItPlay : Boolean = true

    private var _trackTitle = MutableLiveData<String>()
    val trackTitle :LiveData<String> = _trackTitle

    private var _bookId = MutableLiveData<String>()
    val bookId :LiveData<String> = _bookId

    fun playPrevious(){
        _previousTrackClicked.value = Event(true)
    }

    fun playNext(){
        _nextTrackClicked.value = Event(true)
    }

    fun playPause(){
        shouldItPlay = !shouldItPlay
        _previousTrackClicked.value = Event(shouldItPlay)
    }

    fun setTrackTitle(title : String?){
        _trackTitle.value = title?:"UNKNOWN"
    }

    fun setBookId(bookId:String){
        _bookId.value = bookId
    }
}