package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.common.base.network.NetworkResult

interface EnhanceDetailsRepository {
    suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int)
    fun getSearchBooksList(): LiveData<List<WebDocument>>
    fun getBookListWithRanks(bookTitle:String,bookAuthor:String): LiveData<List<Pair<Int, WebDocument>>>

    fun networkStatus() : NetworkResult

    suspend fun fetchBookDetails(bookUrl:String)
    fun getBookDetails():LiveData<BookDetails>

    fun registerNetworkResponse(listener: NetworkResponseListener)
    fun unRegisterNetworkResponse()
}