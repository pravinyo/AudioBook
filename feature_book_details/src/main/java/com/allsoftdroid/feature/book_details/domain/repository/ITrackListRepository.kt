package com.allsoftdroid.feature.book_details.domain.repository

import androidx.lifecycle.LiveData
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel

interface ITrackListRepository{
    suspend fun loadTrackListData(format: TrackFormat)
    fun  getTrackList() : LiveData<List<AudioBookTrackDomainModel>>
}