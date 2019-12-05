package com.allsoftdroid.feature.book_details.data.databaseExtension

import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import com.allsoftdroid.feature.book_details.data.model.toDatabaseModel
import com.allsoftdroid.feature.book_details.data.network.response.GetAudioBookMetadataResponse
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


/**
 * Load the database with the provided metadata of Book Instance
 */
class SaveMetadataInDatabase(metadataDao: MetadataDao) : SaveInDatabase<MetadataDao,SaveMetadataInDatabase> {
    override var mDao: MetadataDao = metadataDao
    private lateinit var mData : GetAudioBookMetadataResponse

    override fun addData(data: Any) = addResponse(data as GetAudioBookMetadataResponse)

    companion object{
        fun setup(metadataDao: MetadataDao) = SaveMetadataInDatabase(metadataDao)
    }

    private fun addResponse(response: GetAudioBookMetadataResponse) : SaveMetadataInDatabase{
        this.mData = response
        return this
    }

    override suspend fun execute(){

        //safe check performed
        val result = mData

        val metadata = DatabaseMetadataEntity(
            identifier = result.metadata.identifier,
            creator = result.metadata.creator,
            date = result.metadata.date,
            description = result.metadata.description,
            licenseUrl = result.metadata.licenseurl,
            tag = result.metadata.subject,
            title = result.metadata.title,
            release_year = result.metadata.publicdate,
            runtime = result.metadata.runtime?:"NA"
        )

        val trackList = ArrayList<DatabaseTrackEntity>()

        //scan the list and build the new track list to be inserted in the database
        val tracks = result.files.filter {
            it.format.toLowerCase(Locale.getDefault()).contains("mp3")
        }

        for(track in tracks){
            trackList.add(track.toDatabaseModel(metadata.identifier))
        }

        val album = DatabaseAlbumEntity(
            identifier = result.metadata.identifier,
            albumName = tracks[0].album,
            creator = metadata.creator
        )


        mDao.insertMetadata(metadata)
        Timber.d("Metadata loaded")
        mDao.insertAlbum(album)
        Timber.d("Album details loaded")
        mDao.insertAllTracks(trackList)
        Timber.d("#${trackList.size} tracks loaded in the DB")

        val list = mDao.getTrackDetails(metadata_id = metadata.identifier).value

        list?.forEach {
            Timber.d(it.trackAlbum_id)
            Timber.d(it.trackTitle)
        }

        list?: emptyList()
    }
}
