package com.allsoftdroid.database.metadataCacheDB

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.database.getOrAwaitValue
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class MetadataDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var metadataDao: MetadataDao
    private lateinit var db: AudioBookDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        db = Room.inMemoryDatabaseBuilder(context, AudioBookDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()

        metadataDao = db.metadataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun test_MetadataDatabase_Book_insertAndGetSuccess() = runBlockingTest{
        val bookMetadata = DatabaseMetadataEntity(
            identifier = "identifier",
            title = "sample",
            creator = "Pravin",
            date = System.currentTimeMillis().toString(),
            description = "random",
            licenseUrl = "None",
            tag = "novel",
            release_year = "2020",
            runtime = "1"
        )

        metadataDao.insertMetadata(bookMetadata)
        val last = metadataDao.getMetadata(bookMetadata.identifier).getOrAwaitValue()

        Assert.assertThat(last, notNullValue())
        Assert.assertThat(last.identifier, `is`(bookMetadata.identifier))
        Assert.assertThat(last.title, `is`(bookMetadata.title))
        Assert.assertThat(last.creator, `is`(bookMetadata.creator))
        Assert.assertThat(last.date, `is`(bookMetadata.date))
    }

    @Test
    @Throws(Exception::class)
    fun test_MetadataDatabase_Book_insertAndDeleteSuccess() = runBlockingTest{
        val bookMetadata = DatabaseMetadataEntity(
            identifier = "identifier",
            title = "sample",
            creator = "Pravin",
            date = System.currentTimeMillis().toString(),
            description = "random",
            licenseUrl = "None",
            tag = "novel",
            release_year = "2020",
            runtime = "1"
        )

        metadataDao.insertMetadata(bookMetadata)
        metadataDao.removeMetadata(bookMetadata.identifier)

        val result = metadataDao.getMetadata(bookMetadata.identifier).getOrAwaitValue()

        Assert.assertThat(result, nullValue())
    }

}