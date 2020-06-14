package com.allsoftdroid.feature_book.data.model

import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

internal data class AudioBookDataModel(
    val identifier: String,
    val title: String,
    val creator: Any?,
    val date: String,
    val addeddate:String?
)

internal fun AudioBookDataModel.toDomainModel(): AudioBookDomainModel {

    return AudioBookDomainModel(
        mId = this.identifier,
        title = this.title,
        creator = this.creator?.toString()?:"N/A",
        date = this.date,
        addeddate = this.addeddate
    )
}


internal fun AudioBookDataModel.toDatabaseModel(): DatabaseAudioBook {

    return DatabaseAudioBook(
        identifier = this.identifier,
        title = this.title,
        creator = this.creator?.toString()?:"N/A",
        date = this.date,
        addeddate = this.addeddate
    )
}