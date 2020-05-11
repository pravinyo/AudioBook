package com.allsoftdroid.feature.book_details.utils

import com.allsoftdroid.feature.book_details.data.model.ListenLaterDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository

internal class FakeListenLaterRepository : IListenLaterRepository {

    var listenLater:ListenLaterDomainModel?=null

    override suspend fun isAddedToListenLater(bookId: String): Boolean {
        if(listenLater!=null){
            return listenLater!!.bookId == bookId
        }
        return false
    }

    override suspend fun addToListenLater(listenLater: ListenLaterDomainModel) {
        this.listenLater = listenLater
    }

    override suspend fun removeListenLater(bookId: String) {
        this.listenLater = null
    }
}