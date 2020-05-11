package com.allsoftdroid.feature.book_details.domain.repository

import com.allsoftdroid.feature.book_details.data.model.ListenLaterDomainModel

internal interface IListenLaterRepository {
    suspend fun isAddedToListenLater(bookId: String):Boolean
    suspend fun addToListenLater(listenLater : ListenLaterDomainModel)
    suspend fun removeListenLater(bookId:String)
}