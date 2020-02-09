package com.allsoftdroid.feature.book_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import com.allsoftdroid.feature.book_details.data.repository.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.utils.NetworkState

interface AudioBookMetadataRepository {

    suspend fun loadMetadata()
    fun getMetadata() : LiveData<AudioBookMetadataDomainModel>
    fun getBookId() : String

    suspend fun loadTrackListData(format:TrackFormat)
    fun  getTrackList() : LiveData<List<AudioBookTrackDomainModel>>

    fun networkResponse():Variable<Event<NetworkState>>
}