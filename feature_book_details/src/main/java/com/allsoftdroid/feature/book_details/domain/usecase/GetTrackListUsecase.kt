package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository

class GetTrackListUsecase(
    private val metadataRepository: AudioBookMetadataRepository
){
    suspend fun loadTrackListData() = metadataRepository.loadTrackListData()

    fun getTrackListData() = metadataRepository.getTrackList()
}