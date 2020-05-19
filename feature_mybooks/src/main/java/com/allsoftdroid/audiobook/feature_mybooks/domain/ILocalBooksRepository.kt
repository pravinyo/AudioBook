package com.allsoftdroid.audiobook.feature_mybooks.domain

import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel

interface ILocalBooksRepository {
    suspend fun getAllBooks():List<LocalBookDomainModel>

    suspend fun removeBook(identifier:String)

    suspend fun deleteAllChapters(identifier: String)
}