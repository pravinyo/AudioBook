package com.allsoftdroid.feature_book.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

internal interface AudioBookRepository {

    suspend fun searchAudioBooks()
    suspend fun getAudioBooks() : LiveData<List<AudioBookDomainModel>>
}