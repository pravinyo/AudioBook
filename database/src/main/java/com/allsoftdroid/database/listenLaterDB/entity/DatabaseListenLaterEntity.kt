package com.allsoftdroid.database.listenLaterDB.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ListenLater_Table")
data class DatabaseListenLaterEntity (

    @PrimaryKey
    @ColumnInfo(name = "book_id")
    var identifier : String,

    var title : String,

    @ColumnInfo(name = "authors")
    var author : String,

    @ColumnInfo(name = "play_time")
    var duration : String,

    @ColumnInfo(name = "timestamp")
    var timeStamp : String
)