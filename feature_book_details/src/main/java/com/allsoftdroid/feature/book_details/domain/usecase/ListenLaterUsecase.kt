package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.feature.book_details.data.model.ListenLaterDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository

internal class ListenLaterUsecase(
    private val listenLaterRepository: IListenLaterRepository
) {

    suspend fun addToListenLater(bookId:String,title:String,author:String,duration:String){
        val listenLaterDomainModel = ListenLaterDomainModel(bookId = bookId,
        bookName = title,
        bookAuthor = author,
        bookDuration = duration)

        listenLaterRepository.addToListenLater(listenLaterDomainModel)
    }

    suspend fun isAdded(bookId: String) = listenLaterRepository.isAddedToListenLater(bookId)

    suspend fun remove(bookId: String) = listenLaterRepository.removeListenLater(bookId)
}