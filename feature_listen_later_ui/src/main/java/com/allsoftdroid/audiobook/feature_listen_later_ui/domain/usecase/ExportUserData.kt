package com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase

import android.os.ParcelFileDescriptor
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.toExternalModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IExportUserDataRepository
import com.allsoftdroid.database.listenLaterDB.ListenLaterDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class ExportUserData(
    private val listenLaterDao: ListenLaterDao,
    private val exportUserDataRepository: IExportUserDataRepository) {

    suspend fun exportToPath(path:String):Flow<Boolean>{
        var result:Flow<Boolean> = flow {}

        withContext(Dispatchers.IO){
            result = try {
                val dbList = listenLaterDao.getBooksInFIFO().map {
                    it.toExternalModel()
                }
                Timber.d("FIFO:$dbList")

                exportUserDataRepository.toFile(path,dbList)
                flow { emit(true) }
            }catch (exception:Exception){
                exception.printStackTrace()
                flow { emit(false) }
            }
        }

        return result
    }

    suspend fun exportToPath(pfd: ParcelFileDescriptor):Flow<Boolean>{
        var result:Flow<Boolean> = flow {}

        withContext(Dispatchers.IO){
            result = try {
                val dbList = listenLaterDao.getBooksInFIFO().map {
                    it.toExternalModel()
                }
                Timber.d("FIFO:$dbList")

                exportUserDataRepository.toFile(pfd,dbList)
                flow { emit(true) }
            }catch (exception:Exception){
                exception.printStackTrace()
                flow { emit(false) }
            }
        }

        return result
    }
}