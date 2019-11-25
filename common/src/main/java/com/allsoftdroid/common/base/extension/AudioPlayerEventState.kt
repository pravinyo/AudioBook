package com.allsoftdroid.common.base.extension


sealed class AudioPlayerEventState

data class PlayingState(
    val playingItemIndex:Int,
    val action_need : Boolean
) : AudioPlayerEventState()