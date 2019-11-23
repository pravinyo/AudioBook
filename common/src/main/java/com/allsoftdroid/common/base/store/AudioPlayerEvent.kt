package com.allsoftdroid.common.base.store

import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.AudioPlayerEventState

sealed class AudioPlayerEvent

data class Next(val result:AudioPlayerEventState) : AudioPlayerEvent()
data class Previous(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class Play(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class Pause(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class Initial(val default: AudioPlayerEventState):AudioPlayerEvent()

data class PlaySelectedTrack(val trackList : List<AudioPlayListItem>,val bookId:String, val position:Int) : AudioPlayerEvent()
data class TrackDetails(val trackTitle:String,val bookId: String, val position: Int):AudioPlayerEvent()
