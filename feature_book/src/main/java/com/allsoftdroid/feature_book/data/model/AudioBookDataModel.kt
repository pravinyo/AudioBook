package com.allsoftdroid.feature_book.data.model

import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel

internal data class AudioBookDataModel(
    val identifier: String,
    val title: String,
    val creator: String?,
    val date: String
)

internal fun AudioBookDataModel.toDomainModel(): AudioBookDomainModel {

    return AudioBookDomainModel(
        mId = this.identifier,
        title = this.title,
        creator = this.creator,
        date = this.date
    )
}
