package com.allsoftdroid.database.networkCacheDB

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.allsoftdroid.database.common.AudioBookDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class NetworkCacheDBTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var networkCacheDao: NetworkCacheDao
    private lateinit var db: AudioBookDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        db = Room.inMemoryDatabaseBuilder(context, AudioBookDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()

        networkCacheDao = db.networkDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun test_MetadataDatabase_Book_insertAndGetSuccess() = runBlockingTest{
        val networkResponse  = DatabaseNetworkResponseEntity(
            identifier = "identifier",
            networkResponse = "this is network data."
        )

        networkCacheDao.insertResponse(networkResponse)

        val last = networkCacheDao.getNetworkResponse(networkResponse.identifier).first()

        assertThat(last,`is`(networkResponse.networkResponse))
    }

    @Test
    @Throws(Exception::class)
    fun test_MetadataDatabase_Book_insertAndDeleteSuccess() = runBlockingTest{
        val networkResponse  = DatabaseNetworkResponseEntity(
            identifier = "identifier",
            networkResponse = "this is network data."
        )

        networkCacheDao.insertResponse(networkResponse)
        networkCacheDao.removeResponse(networkResponse.identifier)

        val last = networkCacheDao.getNetworkResponse(networkResponse.identifier).first()

        assertThat(last, nullValue())
    }

}