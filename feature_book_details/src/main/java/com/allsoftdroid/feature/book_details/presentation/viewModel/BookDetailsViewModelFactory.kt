package com.allsoftdroid.feature.book_details.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature.book_details.data.repository.AudioBookMetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase


@Suppress("UNCHECKED_CAST")
/**
 * Check for the Specified ViewModel existence and return it's instance if already implemented
 * Otherwise return exception
 */
class BookDetailsViewModelFactory(private val application: Application,private val bookId:String): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailsViewModel::class.java)) {

            //database
            val database = AudioBookDatabase.getDatabase(application)
            //repository reference
            val metadataRepository = AudioBookMetadataRepositoryImpl(database.metadataDao(),bookId)


            //Book metadata use case
            val getMetadataUsecase = GetMetadataUsecase(metadataRepository)


            //Book Track list usecase
            val getTrackListUsecase = GetTrackListUsecase(metadataRepository)

            //Use case handler
            val useCaseHandler  = UseCaseHandler.getInstance()

            //sharedPref Dependency
            val sharedPreferencesRepositoryImpl = BookDetailsSharedPreferencesRepositoryImpl.create(application)

            return BookDetailsViewModel(
                application = application,
                sharedPreferenceRepository = sharedPreferencesRepositoryImpl,
                useCaseHandler = useCaseHandler,
                getMetadataUsecase = getMetadataUsecase,
                getTrackListUsecase = getTrackListUsecase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}