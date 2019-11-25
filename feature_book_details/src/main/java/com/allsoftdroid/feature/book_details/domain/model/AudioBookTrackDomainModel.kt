package com.allsoftdroid.feature.book_details.domain.model

import com.allsoftdroid.common.base.extension.AudioPlayListItem

data class AudioBookTrackDomainModel(
    var isPlaying:Boolean = false,
    override val filename : String,
    override val title : String?,
    val trackNumber : Int?,
    val trackAlbum : String?,
    val length: String?,
    val format: String?,
    val size : String?
) : AudioPlayListItem