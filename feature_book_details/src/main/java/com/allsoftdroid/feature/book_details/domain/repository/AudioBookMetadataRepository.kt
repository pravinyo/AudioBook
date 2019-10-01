package com.allsoftdroid.feature.book_details.domain.repository

import androidx.lifecycle.LiveData

interface AudioBookMetadataRepository {

    suspend fun loadMetadataForBookId(bookId : String)
    suspend fun getMetadata()
}