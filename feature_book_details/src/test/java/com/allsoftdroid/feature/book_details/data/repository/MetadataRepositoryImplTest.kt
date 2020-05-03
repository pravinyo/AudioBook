package com.allsoftdroid.feature.book_details.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.allsoftdroid.common.test.MainCoroutineRule
import com.allsoftdroid.common.test.getOrAwaitValue
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataService
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.utils.FakeMetadataSource
import com.allsoftdroid.feature.book_details.utils.FakeRemoteMetadataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock


class MetadataRepositoryImplTest{

    private lateinit var metadataDao : MetadataDao
    private lateinit var remoteMetaDataSource: ArchiveMetadataService
    private lateinit var metadataRepository: IMetadataRepository
    private lateinit var saveInDatabase: SaveInDatabase<MetadataDao, SaveMetadataInDatabase>

    private val bookId = "remoteBookService"

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setup(){
        metadataDao =
            FakeMetadataSource()
        remoteMetaDataSource =
            FakeRemoteMetadataSource()
        saveInDatabase = mock(SaveInDatabase::class.java) as SaveInDatabase<MetadataDao, SaveMetadataInDatabase>

        metadataRepository = MetadataRepositoryImpl(
            metadataDao = metadataDao,
            bookId = bookId,
            metadataDataSource = remoteMetaDataSource,
            saveInDatabase = saveInDatabase
        )

    }

    @Test
    fun loadMetadata_checkBookId_returnsMetadata(){

        mainCoroutineRule.runBlockingTest {
            val result = metadataRepository.getBookId()
            assertThat(result,IsEqual(bookId))
        }
    }

    @Test
    fun loadMetadata_fetchCall_returnsMetadata(){
        runBlocking {
            metadataRepository.loadMetadata()
            stubDataToDao()
            val result = metadataRepository.getMetadata().getOrAwaitValue()
            assertThat(result.identifier,IsEqual(bookId))
        }
    }

    private fun stubDataToDao() {
        metadataDao.insertMetadata(
            DatabaseMetadataEntity(
                identifier = bookId,
                creator = "pravin",
                date = "2020-04-28",
                description = "sample test",
                licenseUrl = "url",
                tag = "book",
                title = "Book read",
                release_year = "2020",
                runtime = "2"
            ))
    }
}