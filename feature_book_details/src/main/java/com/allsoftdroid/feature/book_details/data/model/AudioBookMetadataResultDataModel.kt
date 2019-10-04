package com.allsoftdroid.feature.book_details.data.model

internal data class AudioBookMetadataResultDataModel(
    val files : List<AudioBookTrackDataModel>,
    val item_size : String,
    val metadata : AudioBookMetadataDataModel
)