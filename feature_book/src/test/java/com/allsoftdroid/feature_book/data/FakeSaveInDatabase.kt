package com.allsoftdroid.feature_book.data

import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.feature_book.data.model.AudioBookDataModel
import com.allsoftdroid.feature_book.data.model.toDatabaseModel
import timber.log.Timber

class FakeSaveInDatabase(private val dao : AudioBookDao) : SaveInDatabase<AudioBookDao, FakeSaveInDatabase> {
    override var mDao: AudioBookDao = dao

    private lateinit var mBookList: List<AudioBookDataModel>

    override fun addData(data: Any): FakeSaveInDatabase {
        mBookList = (data as List<*>).filterIsInstance<AudioBookDataModel>()

        return this
    }

    override suspend fun execute() {
        val bookList : MutableList<DatabaseAudioBook> = ArrayList()

        //scan the list and build the new to be inserted in the database
        for(element in mBookList){
            val book: AudioBookDataModel = element

            Timber.i(book.identifier)
            Timber.i(book.title)

            bookList.add(book.toDatabaseModel())
        }
    }
}