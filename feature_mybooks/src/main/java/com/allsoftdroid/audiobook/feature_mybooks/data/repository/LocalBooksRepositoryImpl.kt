package com.allsoftdroid.audiobook.feature_mybooks.data.repository

import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import com.allsoftdroid.audiobook.feature_mybooks.domain.ILocalBooksRepository

class LocalBooksRepositoryImpl : ILocalBooksRepository {

    private val list:List<LocalBookDomainModel> = emptyList()

    override suspend fun getAllBooks(): List<LocalBookDomainModel> {
        return list
    }

    override suspend fun removeBook(identifier: String) {

    }

    override suspend fun deleteAllChapters(identifier: String) {

    }
}