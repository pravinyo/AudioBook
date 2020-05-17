package com.allsoftdroid.audiobook.feature_listen_later_ui.domain

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel

interface IListenLaterRepository {
    /**
     * Remove [ListenLaterItemDomainModel] from listen later DB using identifier
     * @param identifier
     * Unique id assigned to each book
     */
    suspend fun removeBookById(identifier:String)

    /**
     * Return list of [ListenLaterItemDomainModel] in last in first out fashion from DB
     */
    suspend fun getBooksInLIFO(): List<ListenLaterItemDomainModel>

    /**
     * Return list of [ListenLaterItemDomainModel] in fist in first out fashion from DB
     */
    suspend fun getBooksInFIFO():List<ListenLaterItemDomainModel>

    /**
     * Return list of [ListenLaterItemDomainModel] in short playtime first
     */
    suspend fun getBooksInOrderOfLength():List<ListenLaterItemDomainModel>
}