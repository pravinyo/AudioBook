package com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")

class MiniPlayerViewModelFactory(private val application: Application): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiniPlayerViewModel::class.java)) {

            return MiniPlayerViewModel(application = application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}