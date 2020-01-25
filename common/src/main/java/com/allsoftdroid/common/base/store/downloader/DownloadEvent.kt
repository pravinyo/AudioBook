package com.allsoftdroid.common.base.store.downloader

sealed class DownloadEvent{
    abstract val bookId:String
    abstract val chapterIndex:Int
}

data class Download(
    val url:String,
    val name:String,
    val description:String,
    val subPath:String,
    override val bookId:String,
    val chapter:String,
    override val chapterIndex:Int):DownloadEvent()

data class Downloading(
    override val bookId: String,
    override val chapterIndex: Int,
    val downloadId:Long) : DownloadEvent()

data class Downloaded(
    override val bookId: String,
    override val chapterIndex: Int) : DownloadEvent()

data class Failed(
    override val bookId: String,
    override val chapterIndex: Int,
    val message:String) : DownloadEvent()

data class Cancel(
    override val bookId: String,
    override val chapterIndex: Int,
    val downloadId:Long) : DownloadEvent()

data class DownloadNothing(override val bookId: String="",override val chapterIndex: Int=-1) : DownloadEvent()