package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain

interface EnhanceDetailsRepository {
    suspend fun searchBookDetails()
    suspend fun fetchBookDetails(bookUrl:String)
}