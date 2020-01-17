package com.allsoftdroid.audiobook.domain.model

data class LastPlayedTrack(
    val title : String,
    val position:Int,
    val bookId:String,
    val bookName:String
)