package com.allsoftdroid.audiobook.feature_mybooks.domain

import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import timber.log.Timber

class LocalBookListUsecase(
    private val localBooksRepository: ILocalBooksRepository,
    private val bookMetadataRepository: IBookMetadataRepository
) {

    suspend fun getBookList():List<LocalBookDomainModel>{

        val localBooks = mutableListOf<LocalBookDomainModel>()

        val localFiles = localBooksRepository.getLocalBookFiles()
        Timber.d("local files size is ${localFiles.size}")

        for (file in localFiles){

            val metadata = bookMetadataRepository.getBookMetadata(file.identifier)

            Timber.d("File Identifier is : ${file.identifier}")
            Timber.d("Metadata is : ${metadata.title}")
            Timber.d("File is : ${file.filePath}")

            if (metadata.title.isNotEmpty()){
                localBooks.add(
                    LocalBookDomainModel(
                        bookTitle = metadata.title,
                        bookIdentifier = file.identifier,
                        bookAuthor = metadata.author,
                        bookChaptersDownloaded = file.filePath.size,
                        totalChapters = metadata.totalTracks,
                        fileNames = file.filePath
                    ))
            }
        }

        return localBooks
    }

    suspend fun removeBook(bookId:String){
        localBooksRepository.removeBook(bookId)
    }

    suspend fun removeChapters(bookId: String){
        localBooksRepository.deleteAllChapters(bookId)
    }
}