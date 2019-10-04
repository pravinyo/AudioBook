package com.allsoftdroid.feature.book_details.data.network.response

import com.allsoftdroid.feature.book_details.data.model.AudioBookMetadataDataModel
import com.allsoftdroid.feature.book_details.data.model.AudioBookTrackDataModel

internal data class GetAudioBookMetadataResponse(
    val files : List<AudioBookTrackDataModel>,
    val item_size : String,
    val metadata : AudioBookMetadataDataModel
)