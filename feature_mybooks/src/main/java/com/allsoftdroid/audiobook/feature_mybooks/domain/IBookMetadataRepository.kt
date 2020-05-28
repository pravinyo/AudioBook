package com.allsoftdroid.audiobook.feature_mybooks.domain

import com.allsoftdroid.audiobook.feature_mybooks.data.model.BookMetadata

interface IBookMetadataRepository {
    suspend fun getBookMetadata(identifier:String):BookMetadata
}