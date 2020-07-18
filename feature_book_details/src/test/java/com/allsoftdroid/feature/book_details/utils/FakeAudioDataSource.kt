package com.allsoftdroid.feature.book_details.utils

import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeMetadataSource(private var _metadata: DatabaseMetadataEntity,
                         private var _albumEntity: DatabaseAlbumEntity,
                         private var _tracks: List<DatabaseTrackEntity>) : MetadataDao {



    override fun getMetadata(bookId: String): Flow<DatabaseMetadataEntity> {
        return flow { emit(_metadata) }
    }

    override fun getMetadataNonLive(bookId: String): DatabaseMetadataEntity {
        return _metadata
    }

    override fun getAlbumDetails(metadata_id: String): Flow<DatabaseAlbumEntity> {

        return flow { emit(_albumEntity) }
    }

    override fun getTrackDetails(metadata_id: String): Flow<List<DatabaseTrackEntity>> {
        return flow { emit(emptyList()) }
    }

    override fun getTrackDetails(
        metadata_id: String,
        formatContains: String
    ): Flow<List<DatabaseTrackEntity>> {
        return flow { emit(_tracks) }
    }

    override fun getTrackDetailsNonLive(
        metadata_id: String,
        formatContains: String
    ): List<DatabaseTrackEntity> {
        return _tracks
    }

    override fun getTrackDetailsVBR(metadata_id: String): Flow<List<DatabaseTrackEntity>> {
        return flow { emit(_tracks) }
    }

    override fun insertMetadata(metadataEntity: DatabaseMetadataEntity) {
        _metadata = metadataEntity
    }

    override fun insertAlbum(albumEntity: DatabaseAlbumEntity) {
        _albumEntity = albumEntity
    }

    override fun insertAllTracks(trackEntityList: List<DatabaseTrackEntity>) {
        _tracks = trackEntityList
    }

    override fun removeMetadata(metadata_id: String): Int {
        _metadata = DatabaseMetadataEntity("","","","","","","","","")
        return 0
    }

    override fun removeAll(): Int {
        _albumEntity = DatabaseAlbumEntity("","","")
        _metadata = DatabaseMetadataEntity("","","","","","","","","")
        _tracks = emptyList()

        return 0
    }
}