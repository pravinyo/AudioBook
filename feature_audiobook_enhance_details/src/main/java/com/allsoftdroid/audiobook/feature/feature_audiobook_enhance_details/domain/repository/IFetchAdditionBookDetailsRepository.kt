package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails

interface IFetchAdditionBookDetailsRepository:
    INetworkBaseRepository {
    suspend fun fetchBookDetails(bookUrl:String)
    fun getBookDetails(): LiveData<BookDetails>
}