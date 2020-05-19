package com.allsoftdroid.audiobook.feature_mybooks.data.repository

import com.allsoftdroid.audiobook.feature_mybooks.data.model.BookMetadata
import com.allsoftdroid.audiobook.feature_mybooks.domain.IBookMetadataRepository
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookMetadataRepositoryImpl(
    private val metadataDao: MetadataDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
):IBookMetadataRepository {
    override suspend fun getBookMetadata(identifier: String): BookMetadata  =
        withContext(dispatcher){
            val metadata = metadataDao.getMetadata(identifier)
            val tracks = metadataDao.getTrackDetails(metadata_id = identifier,formatContains = "64")

            metadata.value?.let { dbMetadata ->
                tracks.value?.let { dbTracks->
                    return@withContext BookMetadata(title = dbMetadata.title,author = dbMetadata.creator,totalTracks = dbTracks.size)
                }
            }
            return@withContext BookMetadata(title ="",author = "",totalTracks = 0)
        }
}