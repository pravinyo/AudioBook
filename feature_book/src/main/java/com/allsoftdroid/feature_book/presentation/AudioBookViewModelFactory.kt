package com.allsoftdroid.feature_book.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase


@Suppress("UNCHECKED_CAST")
internal class AudioBookViewModelFactory(private val getAlbumListUseCase: GetAudioBookListUsecase, private val application: Application): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioBookViewModel::class.java)) {
            return AudioBookViewModel(getAlbumListUseCase,application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}