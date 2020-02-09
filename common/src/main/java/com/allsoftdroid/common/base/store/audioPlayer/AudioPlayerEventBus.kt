package com.allsoftdroid.common.base.store.audioPlayer

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState

class AudioPlayerEventBus {

    companion object{

        fun getEventBusInstance() = AudioPlayerEventStore.getInstance(Event(
            EmptyEvent(
                PlayingState(
                    playingItemIndex = 0,
                    action_need = false
                )
            )
        ))
    }
}