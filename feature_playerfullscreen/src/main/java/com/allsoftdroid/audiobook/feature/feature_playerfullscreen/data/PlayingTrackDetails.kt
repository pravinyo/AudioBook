package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data

data class PlayingTrackDetails(
    val bookIdentifier : String,
    val bookTitle:String,
    val trackName: String,
    var chapterIndex:Int,
    val totalChapter:Int
)