package com.allsoftdroid.feature.book_details.presentation.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity

class FakeMetadataSource(private val _metadataLiveData: MutableLiveData<DatabaseMetadataEntity> = MutableLiveData(),
                          private val _albumEntity: MutableLiveData<DatabaseAlbumEntity> = MutableLiveData(),
                          private val _tracks: MutableLiveData<List<DatabaseTrackEntity>> = MutableLiveData()) : MetadataDao {



    override fun getMetadata(bookId: String): LiveData<DatabaseMetadataEntity> {
        return _metadataLiveData
    }

    override fun getAlbumDetails(metadata_id: String): LiveData<DatabaseAlbumEntity> {

        return _albumEntity
    }

    override fun getTrackDetails(metadata_id: String): LiveData<List<DatabaseTrackEntity>> {
        val tracks = MutableLiveData<List<DatabaseTrackEntity>>()
        return tracks
    }

    override fun getTrackDetails(
        metadata_id: String,
        formatContains: String
    ): LiveData<List<DatabaseTrackEntity>> {
        return _tracks
    }

    override fun getTrackDetailsVBR(metadata_id: String): LiveData<List<DatabaseTrackEntity>> {
        return _tracks
    }

    override fun insertMetadata(metadataEntity: DatabaseMetadataEntity) {
        _metadataLiveData.value = metadataEntity
    }

    override fun insertAlbum(albumEntity: DatabaseAlbumEntity) {
        _albumEntity.value = albumEntity
    }

    override fun insertAllTracks(trackEntityList: List<DatabaseTrackEntity>) {
        _tracks.value = trackEntityList
    }

    override fun removeMetadata(metadata_id: String): Int {
        _metadataLiveData.value = null
        return 0
    }

    override fun removeAll(): Int {
        _albumEntity.value = null
        _metadataLiveData.value = null
        _tracks.value = null

        return 0
    }
}