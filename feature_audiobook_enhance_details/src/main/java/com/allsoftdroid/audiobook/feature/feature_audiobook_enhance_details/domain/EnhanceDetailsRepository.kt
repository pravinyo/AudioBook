package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain

interface EnhanceDetailsRepository {
    suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int)
    suspend fun fetchBookDetails(bookUrl:String)
}