package com.allsoftdroid.audiobook.feature_listen_later_ui.data.model

import com.allsoftdroid.database.listenLaterDB.entity.DatabaseListenLaterEntity

data class BookMarkDataItem(
    val bookId:String,
    val bookName:String,
    val bookAuthor:String,
    val duration:String?,
    val timeStamp:String
)

fun DatabaseListenLaterEntity.toExternalModel() = BookMarkDataItem(
    bookId = this.identifier,
    bookName = this.title,
    bookAuthor = this.author,
    duration = this.duration,
    timeStamp = this.timeStamp
)

fun BookMarkDataItem.toDatabaseModel() = DatabaseListenLaterEntity(
    identifier = this.bookId,
    title = this.bookName,
    author = this.bookAuthor,
    duration = this.duration?:"",
    timeStamp = this.timeStamp
)