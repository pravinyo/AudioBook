package com.allsoftdroid.feature.book_details.domain.model

import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.feature.book_details.utils.DownloadStatusEvent
import com.allsoftdroid.feature.book_details.utils.NOTHING

data class AudioBookTrackDomainModel(
    var downloadStatus:DownloadStatusEvent = NOTHING,
    var isPlaying:Boolean = false,
    override val filename : String,
    override val title : String?,
    val trackNumber : Int?,
    val trackAlbum : String?,
    val length: String?,
    val format: String?,
    val size : String?,
    val trackId:String
) : AudioPlayListItem