package com.allsoftdroid.feature_book.data.dataSource

import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAudioDataSource(var bookList: MutableList<DatabaseAudioBook> = mutableListOf()) : AudioBookDao {

    private var _bookListLiveData : List<DatabaseAudioBook> = emptyList()

    override fun getBooks(): Flow<List<DatabaseAudioBook>> {
        _bookListLiveData = bookList
        return flow { emit(bookList) }
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