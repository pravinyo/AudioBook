package com.allsoftdroid.feature.book_details.data.model

internal data class AudioBookTrackDataModel(
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