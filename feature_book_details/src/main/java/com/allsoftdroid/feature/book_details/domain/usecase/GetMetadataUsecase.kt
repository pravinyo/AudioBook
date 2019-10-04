package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository

class GetMetadataUsecase (
    private val metadataRepository: AudioBookMetadataRepository
){
    suspend fun execute() = metadataRepository.loadMetadata()

    fun getMetadata() = metadataRepository.getMetadata()
}