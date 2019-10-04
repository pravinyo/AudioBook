package com.allsoftdroid.database.metadataCacheDB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Metadata_Table")
data class DatabaseMetadataEntity(

    @PrimaryKey
    @ColumnInfo(name = "metadata_id")
    var identifier : String,

    @ColumnInfo(name = "uploader")
    var creator : String,

    @ColumnInfo(name = "upload_date")
    var date : String,

    var description : String,

    var licenseUrl : String,

    @ColumnInfo(name = "category")
    var tag : String,

    var title : String,

    var release_year : String,

    var runtime: String
)