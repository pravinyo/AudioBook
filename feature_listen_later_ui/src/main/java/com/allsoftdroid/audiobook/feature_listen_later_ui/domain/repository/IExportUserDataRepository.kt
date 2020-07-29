package com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.BookMarkDataItem

/**
 * Interface for saving user data to specified path
 */
interface IExportUserDataRepository {
    /**
     * This method allow data to be saved at path. on separate thread
     * @param path to where this content be saved
     * @param data which need to be saved
     */
    suspend fun toFile(path:String,data: List<BookMarkDataItem>)
}