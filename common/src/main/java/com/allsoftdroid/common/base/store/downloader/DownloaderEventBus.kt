package com.allsoftdroid.common.base.store.downloader

import com.allsoftdroid.common.base.extension.Event

class DownloaderEventBus {

    companion object{
        fun getEventBusInstance() = DownloadEventStore.getInstance(
            Event(DownloadNothing())
        )
    }
}