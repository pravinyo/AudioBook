package com.allsoftdroid.feature_book.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

interface AudioBookRepository {

    suspend fun searchAudioBooks(page : Int)
    fun getAudioBooks() : LiveData<List<AudioBookDomainModel>>
}