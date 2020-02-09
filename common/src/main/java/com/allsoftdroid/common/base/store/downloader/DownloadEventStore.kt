package com.allsoftdroid.common.base.store.downloader

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.Store
import com.allsoftdroid.common.base.utils.SingletonHolder

class DownloadEventStore private constructor(defaultValue: Event<DownloadEvent>)
    : Store<Event<DownloadEvent>>(defaultValue){
    companion object : SingletonHolder<DownloadEventStore, Event<DownloadEvent>>(creator = ::DownloadEventStore)
}