package com.allsoftdroid.common.base.store

import com.allsoftdroid.common.base.extension.AudioPlayListItem

sealed class AudioPlayerEvent

data class Next(val result:Any) : AudioPlayerEvent()
data class Previous(val result: Any) : AudioPlayerEvent()
data class Play(val result: Any) : AudioPlayerEvent()
data class Pause(val result: Any) : AudioPlayerEvent()
data class Initial(val default: String):AudioPlayerEvent()

data class PlaySelectedTrack(val trackList : List<AudioPlayListItem>,val bookId:String, val position:Int) : AudioPlayerEvent()
data class TrackDetails(val trackTitle:String,val bookId: String):AudioPlayerEvent()
