package com.allsoftdroid.feature.book_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel

interface IMetadataRepository : IBaseRepository{
    suspend fun loadMetadata()
    fun getMetadata() : LiveData<AudioBookMetadataDomainModel>
    fun getBookId() : String
}