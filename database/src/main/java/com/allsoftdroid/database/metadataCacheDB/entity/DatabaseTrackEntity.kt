package com.allsoftdroid.database.metadataCacheDB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "MediaTrack_Table")
data class DatabaseTrackEntity(

    @PrimaryKey
    var track_id : String,

    @ForeignKey(
        entity = DatabaseAlbumEntity::class,
        parentColumns = ["album_metadata_id"],
        childColumns = ["track_album_id"],
        onDelete = ForeignKey.RESTRICT)
    @ColumnInfo(name = "track_album_id")
    var trackAlbum_id : String,

    @ColumnInfo(name = "remote_filename")
    val filename : String,

    @ColumnInfo(name = "title")
    var trackTitle : String?,

    var trackNumber : Int?,

    var length: String?,

    var format: String?,

    var size : String?
)