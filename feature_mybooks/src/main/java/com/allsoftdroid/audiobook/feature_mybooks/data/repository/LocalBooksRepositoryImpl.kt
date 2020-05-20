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
import java.io.File

class LocalBooksRepositoryImpl(
    private val dispatcher: CoroutineDispatcher=Dispatchers.IO,
    private val application: Application,
    private val localFilesForBook:LocalFilesForBook
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
        withContext(dispatcher){
            val rootFolder = ArchiveUtils.getDownloadsRootFolder(application)
            Timber.d("Root Folder is $rootFolder")

            val directory = Environment.getExternalStoragePublicDirectory("$rootFolder/AudioBooks/")
            val books = directory.listFiles()?.filter {
                it.absolutePath.split("/").last() == identifier
            }

            Timber.d("Books to be removed are : $books")

            books?.let {
                it.forEach { folder ->
                    try {
                        deleteRecursive(folder)
                    }catch (e:Exception){
                        Timber.e("Error: can't remove $folder")
                    }
                }
            }
        }
    }

    override suspend fun deleteAllChapters(identifier: String) {
        withContext(dispatcher){
            val removeFiles = localFilesForBook.getDownloadedFilesList(identifier)

            removeFiles?.let {filePaths ->
                Timber.d("Files to be removed are: $filePaths")

                filePaths.forEach {
                    try {
                        val file = File(it)
                        deleteRecursive(file)
                    }catch (e:Exception){
                        Timber.e("Error: can't remove $it")
                    }
                }
            }
        }
    }

    private fun deleteRecursive(fileOrDirectory: File){

        if(fileOrDirectory.isDirectory){
            fileOrDirectory.listFiles()?.forEach {
                deleteRecursive(it)
            }
        }

        fileOrDirectory.delete()
    }
}