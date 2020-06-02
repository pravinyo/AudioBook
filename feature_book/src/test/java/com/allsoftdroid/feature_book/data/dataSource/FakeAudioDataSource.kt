package com.allsoftdroid.feature_book.data.dataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook

class FakeAudioDataSource(var bookList: MutableList<DatabaseAudioBook> = mutableListOf()) : AudioBookDao {

    private var _bookListLiveData = MutableLiveData<List<DatabaseAudioBook>>()

    override fun getBooks(): LiveData<List<DatabaseAudioBook>> {
        _bookListLiveData.value = bookList
        return _bookListLiveData
    }

    override fun getBookBy(identifier: String): DatabaseAudioBook {
        bookList.forEach {
            if(it.identifier == identifier) return it
        }

        return DatabaseAudioBook(identifier="",title = "",creator = "",date = "",addeddate = "")
    }

    override fun insert(book: DatabaseAudioBook) {
        bookList.add(book)
    }

    override fun insertAllList(books: List<DatabaseAudioBook>) {
        bookList.addAll(books)
    }

    override fun deleteAll(): Int {
        val size = bookList.size
        bookList.clear()

        return size
    }

    override fun deleteItem(identifier: String) {
        bookList = bookList.filter {
            it.identifier != identifier
        }.toMutableList()
    }

    override fun getLastBook(): DatabaseAudioBook {
        return bookList[bookList.lastIndex]
    }
}