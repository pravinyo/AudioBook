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
    override suspend fun getBookMetadata(identifier: String): BookMetadata {

       return withContext(dispatcher){
            val metadata = metadataDao.getMetadataNonLive(identifier)
            val tracks = metadataDao.getTrackDetailsNonLive(metadata_id = identifier,formatContains = "64")

           if(metadata==null || tracks.isEmpty()) {
               return@withContext BookMetadata(title ="",author = "",totalTracks =0)
           }else{
               return@withContext BookMetadata(title =metadata.title,author = metadata.creator,totalTracks = tracks.size)
           }
        }
    }
}