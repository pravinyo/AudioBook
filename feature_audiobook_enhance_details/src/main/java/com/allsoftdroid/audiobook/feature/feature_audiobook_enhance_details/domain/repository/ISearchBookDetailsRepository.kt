package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.INetworkBaseRepository

interface ISearchBookDetailsRepository :
    INetworkBaseRepository {
    suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int)
    fun getSearchBooksList(): LiveData<List<WebDocument>>
    fun getBookListWithRanks(bookTitle:String,bookAuthor:String): LiveData<List<Pair<Int, WebDocument>>>
}