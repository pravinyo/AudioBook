package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data

data class PlayingTrackDetails(
    val bookIdentifier : String,
    val bookTitle:String,
    val name: String,
    val chapterIndex:Int,
    val totalChapter:Int
)