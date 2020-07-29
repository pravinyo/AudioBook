package com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.BookMarkDataItem

/**
 * Interface for importing user content to App Listen later DB
 */
interface IImportUserDataRepository {
    /**
     * It load the data from the file at Path and returns list of data.
     * @param path of the file for data import
     * @return list of @[BookMarkDataItem]
     */
    suspend fun fromFile(path:String):List<BookMarkDataItem>
}