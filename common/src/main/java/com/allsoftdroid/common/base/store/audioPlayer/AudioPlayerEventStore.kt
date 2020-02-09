package com.allsoftdroid.common.base.store.audioPlayer

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.Store
import com.allsoftdroid.common.base.utils.SingletonHolder

class AudioPlayerEventStore private constructor(defaultValue: Event<AudioPlayerEvent>)
    : Store<Event<AudioPlayerEvent>>(defaultValue){
    companion object : SingletonHolder<AudioPlayerEventStore, Event<AudioPlayerEvent>>(creator = ::AudioPlayerEventStore)
}