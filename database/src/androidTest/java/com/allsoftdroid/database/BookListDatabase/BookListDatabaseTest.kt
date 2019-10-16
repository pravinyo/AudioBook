package com.allsoftdroid.database.BookListDatabase

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.AudioBookDatabase
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BooksDatabaseTest {

    private lateinit var bookListDao: AudioBookDao
    private lateinit var db: AudioBookDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AudioBookDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()

        bookListDao = db.audioBooksDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun test_BooksDatabase_Users_insertAndGetSuccess() {
        val user = DatabaseAudioBook(
            identifier = "identifier",
            title = "sample",
            creator = "Pravin",
            date = System.currentTimeMillis().toString()
        )

        bookListDao.insert(user)
        val last = bookListDao.getLastBook()
        assertEquals(last?.creator, "Pravin")
    }
}