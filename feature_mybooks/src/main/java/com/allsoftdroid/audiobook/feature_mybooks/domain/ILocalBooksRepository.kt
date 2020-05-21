package com.allsoftdroid.audiobook.feature_mybooks.domain

import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookFiles

interface ILocalBooksRepository {
    suspend fun getLocalBookFiles():List<LocalBookFiles>

    suspend fun removeBook(identifier:String)

    suspend fun deleteAllChapters(identifier: String)
}