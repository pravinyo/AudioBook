package com.allsoftdroid.database.metadataCacheDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity

/**
 * This DAO interface specifies what are the type of database operation can be done.
 * It act as the interface to query database
 */
@Dao
interface MetadataDao{
    /**
     * Get metadata for specified book
     * @param bookId unique identifier of the book
     * @return {@link DatabaseMetadataEntity} has details of the request book
     */
    @Query("SELECT * FROM metadata_table where metadata_id=:bookId")
    fun getMetadata( bookId : String):LiveData<DatabaseMetadataEntity>

    /**
     * Get album details for the specified audio book
     * @param metadata_id unique id given to audio book
     * @return {@link DatabaseAlbumEntity} has details of the album
     */
    @Query("SELECT * FROM Album_Table  where album_metadata_id = :metadata_id")
    fun getAlbumDetails( metadata_id:String):LiveData<DatabaseAlbumEntity>

    /**
     * get list of  media track files for the given album id . here album id is same as metadata id so we will
     * directly use it without writing complex SQL Query
     * @param metadata_id unique id given to audio book
     * @return list of {@link DatabaseTrackEntity} track
     */
    @Query("SELECT * FROM MediaTrack_Table where track_album_id=:metadata_id")
    fun getTrackDetails(metadata_id:String):LiveData<List<DatabaseTrackEntity>>

    /**
     * get list of  media track files for the given album id . here album id is same as metadata id so we will
     * directly use it without writing complex SQL Query
     * @param metadata_id unique id given to audio book
     * @param formatContains keyword that should be part of the string
     * @return list of {@link DatabaseTrackEntity} track
     */
    @Query("SELECT * FROM MediaTrack_Table where track_album_id=:metadata_id and format like '%' || :formatContains || '%'")
    fun getTrackDetails(metadata_id:String,formatContains:String):LiveData<List<DatabaseTrackEntity>>

    /**
     * Insert individual book metadata in the database
     * @param metadataEntity entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMetadata(metadataEntity : DatabaseMetadataEntity)

    /**
     * Insert Album details in the database
     * @param albumEntity
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlbum(albumEntity: DatabaseAlbumEntity)

    /**
     * Insert list of tracks in the database.
     * @param trackEntityList
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTracks(trackEntityList : List<DatabaseTrackEntity>)

    /**
     * Delete the entry of specified media files from the local DB
     * using cascade effect it will remove all the dependent entries from other tables
     * @param metadata_id  unique id given to audio book
     * @return count of entries removed from the DB
     */
    @Query("DELETE FROM METADATA_TABLE WHERE metadata_id=:metadata_id")
    fun removeMetadata(metadata_id:String):Int

    /**
     * Delete all the entries from the METADATA tables
     * ALL the dependent tables will also get cleared due to cascade effect
     */
    @Query("DELETE FROM METADATA_TABLE")
    fun removeAll():Int
}

