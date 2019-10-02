package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository

class GetMetadataUsecase (
    private val metadataRepository: AudioBookMetadataRepository,private val bookId:String
){
    suspend fun execute() = metadataRepository.loadMetadataForBookId(bookId)

    suspend fun getMetadata() = metadataRepository.getMetadata()
}