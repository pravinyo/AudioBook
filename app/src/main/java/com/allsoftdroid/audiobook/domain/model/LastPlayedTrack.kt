package com.allsoftdroid.audiobook.domain.model

/**
 * Data Structure for last Played track
 */
data class LastPlayedTrack(
    val title : String,
    val position:Int,
    val bookId:String,
    val bookName:String
)