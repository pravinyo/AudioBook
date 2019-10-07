package com.allsoftdroid.database.metadataCacheDB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Album_Table")
data class DatabaseAlbumEntity(

    @ForeignKey(
        entity = DatabaseMetadataEntity::class,
        parentColumns = ["metadata_id"],
        childColumns = ["album_metadata_id"],
        onDelete = ForeignKey.RESTRICT)
    @ColumnInfo(name = "album_metadata_id")
    var identifier : String,

    @PrimaryKey
    @ColumnInfo(name = "album_name")
    var albumName:String,

    var creator : String
)