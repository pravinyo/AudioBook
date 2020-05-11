package com.allsoftdroid.feature.book_details.utils

sealed class DownloadStatusEvent

object DOWNLOADING : DownloadStatusEvent()
object DOWNLOADED: DownloadStatusEvent()
object CANCELLED: DownloadStatusEvent()
object NOTHING: DownloadStatusEvent()
data class PROGRESS(val percent:Float) : DownloadStatusEvent()