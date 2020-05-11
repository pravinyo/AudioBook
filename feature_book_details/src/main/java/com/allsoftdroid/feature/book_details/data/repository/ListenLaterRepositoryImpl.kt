package com.allsoftdroid.feature.book_details.data.repository

import com.allsoftdroid.database.listenLaterDB.ListenLaterDao
import com.allsoftdroid.feature.book_details.data.model.ListenLaterDomainModel
import com.allsoftdroid.feature.book_details.data.model.toDatabaseModel
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ListenLaterRepositoryImpl(
    private val listenLaterDao: ListenLaterDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IListenLaterRepository {

    override suspend fun isAddedToListenLater(bookId: String): Boolean {
        val count = withContext(dispatcher){
             listenLaterDao.getListenLaterStatusFor(bookId)
        }

        return count!=0
    }

    override suspend fun addToListenLater(listenLater : ListenLaterDomainModel) {
        withContext(dispatcher){
            listenLaterDao.insertForLater(listenLater.toDatabaseModel())
        }
    }

    override suspend fun removeListenLater(bookId: String) {
        withContext(dispatcher){
            listenLaterDao.removeById(bookId)
        }
    }
}