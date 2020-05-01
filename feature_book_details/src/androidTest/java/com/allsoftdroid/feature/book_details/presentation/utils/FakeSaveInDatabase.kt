package com.allsoftdroid.feature.book_details.presentation.utils

import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.model.AudioBookMetadataDataModel
import timber.log.Timber

class FakeSaveInDatabase(private val dao : MetadataDao) : SaveInDatabase<MetadataDao, FakeSaveInDatabase> {
    override var mDao: MetadataDao = dao

    private lateinit var mBookList: List<AudioBookMetadataDataModel>

    override fun addData(data: Any): FakeSaveInDatabase {
        mBookList = (data as List<*>).filterIsInstance<AudioBookMetadataDataModel>()

        return this
    }

    override suspend fun execute() {

        //scan the list and build the new to be inserted in the database
        for(element in mBookList){
            val book: AudioBookMetadataDataModel = element

            Timber.i(book.identifier)
            Timber.i(book.title)
        }
    }
}