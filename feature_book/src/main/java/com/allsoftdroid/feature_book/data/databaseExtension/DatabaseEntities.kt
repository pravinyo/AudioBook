package com.allsoftdroid.feature_book.data.databaseExtension

import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

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
