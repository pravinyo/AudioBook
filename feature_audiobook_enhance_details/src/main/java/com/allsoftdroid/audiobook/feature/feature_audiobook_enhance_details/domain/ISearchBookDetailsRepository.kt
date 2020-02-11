package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument

interface ISearchBookDetailsRepository : INetworkBaseRepository{
    suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int)
    fun getSearchBooksList(): LiveData<List<WebDocument>>
    fun getBookListWithRanks(bookTitle:String,bookAuthor:String): LiveData<List<Pair<Int, WebDocument>>>
}