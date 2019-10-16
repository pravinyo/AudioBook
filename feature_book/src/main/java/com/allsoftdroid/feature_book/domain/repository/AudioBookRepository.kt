package com.allsoftdroid.feature_book.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

interface AudioBookRepository {
    suspend fun searchAudioBooks()
    fun getAudioBooks() : LiveData<List<AudioBookDomainModel>>
    fun onError():LiveData<Event<Any>>
}