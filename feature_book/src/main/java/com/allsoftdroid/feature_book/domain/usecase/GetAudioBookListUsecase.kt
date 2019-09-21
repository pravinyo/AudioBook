package com.allsoftdroid.feature_book.domain.usecase

import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository

class GetAudioBookListUsecase(
    private val audioBookRep: AudioBookRepository
) {

    suspend fun execute() = audioBookRep.searchAudioBooks()

    suspend fun getAudioBook() = audioBookRep.getAudioBooks()
}