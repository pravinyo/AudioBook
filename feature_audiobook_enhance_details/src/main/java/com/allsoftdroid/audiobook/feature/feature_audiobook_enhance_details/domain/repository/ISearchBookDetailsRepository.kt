package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import kotlinx.coroutines.flow.Flow

interface ISearchBookDetailsRepository :
    INetworkBaseRepository {
    suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int)
    fun getSearchBooksList(): Flow<List<WebDocument>>
    fun getBookListWithRanks(bookTitle:String,bookAuthor:String): Flow<List<Pair<Int, WebDocument>>>
}