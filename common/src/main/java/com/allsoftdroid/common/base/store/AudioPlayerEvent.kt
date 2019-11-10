package com.allsoftdroid.common.base.store

sealed class AudioPlayerEvent

data class Next(val result:Any) : AudioPlayerEvent()
data class Previous(val result: Any) : AudioPlayerEvent()
data class Play(val result: Any) : AudioPlayerEvent()
data class Pause(val result: Any) : AudioPlayerEvent()
