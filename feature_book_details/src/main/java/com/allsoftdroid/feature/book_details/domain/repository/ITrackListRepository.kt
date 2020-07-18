package com.allsoftdroid.feature.book_details.domain.repository

import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import kotlinx.coroutines.flow.Flow

interface ITrackListRepository{
    suspend fun loadTrackListData(format: TrackFormat) : Flow<List<AudioBookTrackDomainModel>>
}