package com.allsoftdroid.audiobook.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event

class MainActivityViewModel(application : Application) : AndroidViewModel(application){

    private val _showMiniPlayer = MutableLiveData<Event<Boolean>>()
    val showPlayer :LiveData<Event<Boolean>> = _showMiniPlayer


    fun playerStatus( showPlayer : Boolean){
        _showMiniPlayer.value = Event(showPlayer)
    }
}