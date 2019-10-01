package com.allsoftdroid.feature.book_details.data.model

internal data class AudioBookMetadataResultDataModel(
    val trackList : AudioBookTrackListDataModel,
    val totalSize : String,
    val metaData : AudioBookMetadataDataModel
)