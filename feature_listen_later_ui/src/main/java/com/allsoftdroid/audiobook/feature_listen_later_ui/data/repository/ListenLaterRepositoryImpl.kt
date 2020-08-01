package com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.toDomainModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IListenLaterRepository
import com.allsoftdroid.database.listenLaterDB.ListenLaterDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListenLaterRepositoryImpl(
    private val listenLaterDao: ListenLaterDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IListenLaterRepository {
    override suspend fun removeBookById(identifier: String) {
        withContext(dispatcher){
            listenLaterDao.removeById(identifier)
        }
    }

    override suspend fun getBooksInLIFO(): List<ListenLaterItemDomainModel> {
        return withContext(dispatcher){
            listenLaterDao.getBooksInLIFO().map {
                it.toDomainModel()
            }
        }
    }

    override suspend fun getBooksInFIFO(): List<ListenLaterItemDomainModel> {
        return withContext(dispatcher){
            listenLaterDao.getBooksInFIFO().map {
                it.toDomainModel()
            }
        }
    }

    override suspend fun getBooksInOrderOfLength(): List<ListenLaterItemDomainModel> {
        return withContext(dispatcher){
            listenLaterDao.getBooksInOrderOfLength().map {
                it.toDomainModel()
            }
        }
    }
}