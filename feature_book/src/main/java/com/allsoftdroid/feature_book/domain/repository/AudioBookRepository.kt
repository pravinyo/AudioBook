package com.allsoftdroid.feature_book.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

interface AudioBookRepository {

    suspend fun fetchBookList(page : Int)
    fun getAudioBooks() : LiveData<List<AudioBookDomainModel>>

    suspend fun searchBookList(query:String,page: Int)
    fun getSearchBooks() : LiveData<List<AudioBookDomainModel>>

    fun registerNetworkResponse(listener: NetworkResponseListener)
    fun unRegisterNetworkResponse()

    fun cancelRequestInFlight()
}