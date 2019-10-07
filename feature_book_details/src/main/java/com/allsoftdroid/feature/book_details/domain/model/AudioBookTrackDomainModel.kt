package com.allsoftdroid.feature.book_details.domain.model

data class AudioBookTrackDomainModel(
    var isPlaying:Boolean = false,
    val filename : String,
    val trackTitle : String?,
    val trackNumber : Int?,
    val trackAlbum : String?,
    val length: String?,
    val format: String?,
    val size : String?
)