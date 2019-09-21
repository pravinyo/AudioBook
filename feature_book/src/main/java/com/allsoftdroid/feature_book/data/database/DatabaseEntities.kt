package com.allsoftdroid.feature_book.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

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
    var date: String?
){
    constructor() : this("","","","")
}

/**
 * Convert database instance into domain model instance
 */

fun List<DatabaseAudioBook>.asBookDomainModel():List<AudioBookDomainModel>{
    return map {
        AudioBookDomainModel(
            mId = it.identifier,
            title = it.title,
            creator = it.creator,
            date = it.date
        )
    }
}

fun DatabaseAudioBook.toBookDomainModel():AudioBookDomainModel{
    return AudioBookDomainModel(
        mId = identifier,
        title = title,
        creator = creator,
        date = date
    )
}