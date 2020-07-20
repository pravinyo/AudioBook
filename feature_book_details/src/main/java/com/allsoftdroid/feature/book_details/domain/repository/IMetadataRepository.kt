package com.allsoftdroid.feature.book_details.domain.repository

import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import kotlinx.coroutines.flow.Flow

interface IMetadataRepository : IBaseRepository{
    suspend fun loadMetadata()
    fun getMetadata() : Flow<AudioBookMetadataDomainModel>
    fun getBookId() : String

    fun cancelRequestInFlight()
}