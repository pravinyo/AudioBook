package com.allsoftdroid.feature_book.data.databaseExtension

import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.feature_book.data.model.AudioBookDataModel
import com.allsoftdroid.feature_book.data.model.toDatabaseModel
import timber.log.Timber

/**
 * Load the database with the provided list of Book Instance
 * It first clears old Books records  from the DB  and reload fresh content
 */

class SaveBookListInDatabase(bookDao: AudioBookDao) : SaveInDatabase<AudioBookDao> {

    override var mDao: AudioBookDao = bookDao
    private lateinit var bookList: List<AudioBookDataModel>

    companion object{
        fun setup(bookDao: AudioBookDao) = SaveBookListInDatabase(bookDao)
    }

    override fun addData(data: Any) = addList(
        (data as List<*>).filterIsInstance<AudioBookDataModel>()
    )

    private fun addList(list: List<AudioBookDataModel>) : SaveBookListInDatabase{
        this.bookList = list
        return this
    }

    override suspend fun execute(){

        //safe check performed
        val result = bookList

        val bookList : MutableList<DatabaseAudioBook> = ArrayList()

        //scan the list and build the new to be inserted in the database
        for(element in result){
            val book: AudioBookDataModel = element

            Timber.i(book.identifier)
            Timber.i(book.title)

            bookList.add(book.toDatabaseModel())
        }

        //remove already existing data
        val deleted=  mDao.deleteAll()
        Timber.i("data deleted $deleted")


        //Insert new Fresh data in the database
        mDao.insertAllList(bookList)
        Timber.i("data loaded ${bookList.size}")

//
//        val inserted = mDao.getBooks()
//        val count = inserted.value?.size
//        Timber.i("data fetching from DB: $count")
//
//        val entry = 10
//        val sampleBook = mDao.getBookBy(bookList[entry].identifier)
//        Timber.i("Data check for #$entry entry : ${sampleBook.title}")
//
//        val list = inserted.value?: emptyList()
//
//        Timber.d("Size is ${list.size}")
    }
}