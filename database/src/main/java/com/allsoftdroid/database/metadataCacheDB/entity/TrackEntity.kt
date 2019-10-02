package com.allsoftdroid.database.metadataCacheDB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "MediaTrack_Table")
data class TrackEntity(

    @ForeignKey(
        entity = AlbumEntity::class,
        parentColumns = ["album_name"],
        childColumns = ["trackAlbum"],
        onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "metadata_id")
    var trackAlbum : String,

    @ColumnInfo(name = "remote_filename")
    val filename : String,

    @ColumnInfo(name = "title")
    val trackTitle : String,

    val trackNumber : Int,

    val length: String,

    val format: String,

    val size : String
)