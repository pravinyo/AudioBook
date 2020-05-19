package com.allsoftdroid.audiobook.feature_mybooks.data.repository

import android.app.Application
import android.os.Environment
import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookFiles
import com.allsoftdroid.audiobook.feature_mybooks.domain.ILocalBooksRepository
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocalBooksRepositoryImpl(
    private val dispatcher: CoroutineDispatcher=Dispatchers.IO,
    private val application: Application
) : ILocalBooksRepository {

    override suspend fun getLocalBookFiles(): List<LocalBookFiles> =
        withContext(dispatcher){
            val localBookFiles = mutableListOf<LocalBookFiles>()

            val rootFolder = ArchiveUtils.getDownloadsRootFolder(application)
            Timber.d("Root Folder is $rootFolder")

            val directory = Environment.getExternalStoragePublicDirectory("$rootFolder/AudioBooks/")
            val bookIds = directory.listFiles()?.map {
                it.absolutePath.split("/").last()
            }

            Timber.d("BookIds are  $bookIds")

            val localFilesForBook = LocalFilesForBook(application)

            bookIds?.map {bookId->
                val files = localFilesForBook.getDownloadedFilesList(bookId)
                if(files.isNullOrEmpty()){
                    localBookFiles.add(LocalBookFiles(identifier = bookId,filePath = emptyList()))
                }else{
                    Timber.i("$bookId : Found ${files.size} files")
                    localBookFiles.add(LocalBookFiles(identifier = bookId,filePath = files))
                }
            }

            return@withContext localBookFiles
        }

    override suspend fun removeBook(identifier: String) {
        //delete sub-directory
    }

    override suspend fun deleteAllChapters(identifier: String) {
        //delete all the files in the sub-directory
    }
}