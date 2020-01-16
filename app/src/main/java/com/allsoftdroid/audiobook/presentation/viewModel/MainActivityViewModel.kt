package com.allsoftdroid.audiobook.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository

class MainActivityViewModel(
    application : Application,
    private val sharedPref: BookDetailsSharedPreferenceRepository) : AndroidViewModel(application){

    private val _showMiniPlayer = MutableLiveData<Event<Boolean>>()
    val showPlayer :LiveData<Event<Boolean>> = _showMiniPlayer

    private var _stopService = MutableLiveData<Event<Boolean>>()
    val stopServiceEvent : LiveData<Event<Boolean>> = _stopService


    fun playerStatus( showPlayer : Boolean){
        _showMiniPlayer.value = Event(showPlayer)
    }

    init {
        _stopService.value = Event(false)
    }

    fun clearSharedPref(){
        sharedPref.clear()
    }

    override fun onCleared() {
        super.onCleared()
        _stopService.value = Event(true)
    }
}