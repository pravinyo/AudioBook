package com.allsoftdroid.feature_book.domain.repository

import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import kotlinx.coroutines.flow.Flow

interface AudioBookRepository {

    suspend fun fetchBookList(page : Int)
    fun getAudioBooks() : Flow<List<AudioBookDomainModel>>

    suspend fun searchBookList(query:String,page: Int)
    fun getSearchBooks() : Flow<List<AudioBookDomainModel>>

    fun registerNetworkResponse(listener: NetworkResponseListener)
    fun unRegisterNetworkResponse()

    fun cancelRequestInFlight()
}