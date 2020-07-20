package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

interface IFetchAdditionBookDetailsRepository:
    INetworkBaseRepository {
    suspend fun fetchBookDetails(bookUrl:String)
}