package com.allsoftdroid.feature.book_details.domain.model

data class AudioBookTrackDomainModel(
    val filename : String,
    val creator : String,
    val trackTitle : String,
    val trackNumber : Int,
    val trackAlbum : String,
    val genre : String,
    val length: String,
    val format: String,
    val size : String
)