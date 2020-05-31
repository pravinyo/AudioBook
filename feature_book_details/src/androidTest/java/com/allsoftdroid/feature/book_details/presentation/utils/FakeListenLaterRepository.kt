package com.allsoftdroid.feature.book_details.presentation.utils

import com.allsoftdroid.feature.book_details.data.model.ListenLaterDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository

internal class FakeListenLaterRepository : IListenLaterRepository {
    override suspend fun isAddedToListenLater(bookId: String): Boolean {
        return true
    }

    override suspend fun addToListenLater(listenLater: ListenLaterDomainModel) {

    }

    override suspend fun removeListenLater(bookId: String) {

    }
}