package com.allsoftdroid.feature.book_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel

interface AudioBookMetadataRepository {

    suspend fun loadMetadata()
    fun getMetadata() : LiveData<AudioBookMetadataDomainModel>
    fun getBookId() : String


    suspend fun loadTrackListData()
    fun  getTrackList() : LiveData<List<AudioBookTrackDomainModel>>
}