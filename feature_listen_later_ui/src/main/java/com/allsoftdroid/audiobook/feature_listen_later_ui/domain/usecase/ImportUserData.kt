package com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase

import android.os.ParcelFileDescriptor
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.BookMarkDataItem
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.toDatabaseModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IImportUserDataRepository
import com.allsoftdroid.database.listenLaterDB.ListenLaterDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class ImportUserData(private val listenLaterDao: ListenLaterDao,
                     private val importUserDataRepository: IImportUserDataRepository) {

    suspend fun fromFile(path:String): Flow<Boolean> {
        var result : Flow<Boolean> = flow {  }

        withContext(Dispatchers.IO){
            result = try {
                val importedData:List<BookMarkDataItem>? = importUserDataRepository.fromFile(path)

                importedData?.let {  bookList->
                    val dbList = bookList.map { it.toDatabaseModel() }

                    if (dbList.isNotEmpty()){
                        dbList.forEach {
                            listenLaterDao.insertForLater(it)
                            Timber.d(it.toString())
                        }
                    }

                    Timber.d("dbList is : $dbList")
                }?: Timber.d("imported data is null")

                flow { emit(true)  }
            }catch (exception:Exception){
                flow { emit(false)  }
            }
        }

        return result
    }

    suspend fun fromFile(pfd: ParcelFileDescriptor): Flow<Boolean> {
        var result : Flow<Boolean> = flow {  }

        withContext(Dispatchers.IO){
            result = try {
                val importedData:List<BookMarkDataItem>? = importUserDataRepository.fromFile(pfd)

                importedData?.let {  bookList->
                    val dbList = bookList.map { it.toDatabaseModel() }

                    if (dbList.isNotEmpty()){
                        dbList.forEach {
                            listenLaterDao.insertForLater(it)
                            Timber.d(it.toString())
                        }
                    }

                    Timber.d("dbList is : $dbList")
                }?: Timber.d("imported data is null")

                flow { emit(true)  }
            }catch (exception:Exception){
                flow { emit(false)  }
            }
        }

        return result
    }
}