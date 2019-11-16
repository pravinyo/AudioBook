package com.allsoftdroid.common.base.store

import com.allsoftdroid.common.base.extension.Event

class AudioPlayerEventBus {

    companion object{

        fun getEventBusInstance() = AudioPlayerEventStore.getInstance(Event(Initial("")))

    }
}