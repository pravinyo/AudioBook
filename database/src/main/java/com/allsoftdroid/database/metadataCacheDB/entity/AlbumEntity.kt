package com.allsoftdroid.database.metadataCacheDB.entity

import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

data class AlbumEntity(

    @ForeignKey(
        entity = MetadataEntity::class,
        parentColumns = ["metadata_id"],
        childColumns = ["metadata_id"],
        onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "metadata_id")
    var identifier : String,

    @PrimaryKey
    @ColumnInfo(name = "album_name")
    var albumName:String,

    var creator : String
)