package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase


@Suppress("UNCHECKED_CAST")
/**
 * Check for the Specified ViewModel existence and return it's instance if already implemented
 * Otherwise return exception
 */
class AudioBookListViewModelFactory(private val application: Application): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioBookListViewModel::class.java)) {

            //database
            val database = AudioBookDatabase.getDatabase(application)
            //repository reference
            val bookRepository = AudioBookRepositoryImpl(database.audioBooksDao())
            //Book list use case
            val getAlbumListUseCase = GetAudioBookListUsecase(bookRepository)
            //Use case handler
            val useCaseHandler  = UseCaseHandler.getInstance()

            return AudioBookListViewModel(application,useCaseHandler = useCaseHandler,getAlbumListUseCase = getAlbumListUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}