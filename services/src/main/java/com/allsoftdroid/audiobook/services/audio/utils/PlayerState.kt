package com.allsoftdroid.audiobook.services.audio.utils

enum class PlayerState{
    SourceError,
    SystemError,

    PlayerFinished,
    PlayerReady,
    PlayerBusy,
    PlayerIdle,
    PlayingNext
}

