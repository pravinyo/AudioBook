package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


@Suppress("UNCHECKED_CAST")
/**
 * Check for the Specified ViewModel existence and return it's instance if already implemented
 * Otherwise return exception
 */
class AudioBookListViewModelFactory(private val application: Application): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioBookListViewModel::class.java)) {
            return AudioBookListViewModel(application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}