package com.allsoftdroid.database.bookListDatabase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.AudioBookDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class BooksDatabaseTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var bookListDao: AudioBookDao
    private lateinit var db: AudioBookDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

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
    fun test_BooksDatabase_Users_insertAndGetSuccess() = runBlockingTest{
        val user = DatabaseAudioBook(
            identifier = "identifier",
            title = "sample",
            creator = "Pravin",
            date = System.currentTimeMillis().toString()
        )

        bookListDao.insert(user)
        val last = bookListDao.getLastBook()
        assertEquals(last.creator, "Pravin")
    }
}