package com.allsoftdroid.feature.book_details.presentation.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


@Suppress("UNCHECKED_CAST")
/**
 * Check for the Specified ViewModel existence and return it's instance if already implemented
 * Otherwise return exception
 */
class BookDetailsViewModelFactory(private val application: Application,private val bookId:String): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailsViewModel::class.java)) {
            return BookDetailsViewModel(application,bookId) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}