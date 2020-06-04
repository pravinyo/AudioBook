package com.allsoftdroid.database.bookListDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data Structure for Book instance in Database
 */

@Entity(tableName = "AudioBook_Table")
data class DatabaseAudioBook(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var identifier: String,

    @ColumnInfo(name = "book_title")
    var title : String,

    var creator : String?,

    @ColumnInfo(name = "published_date")
    var date: String?,

    @ColumnInfo(name = "added_date")
    var addeddate: String?
){
    constructor() : this("","","","","")
}