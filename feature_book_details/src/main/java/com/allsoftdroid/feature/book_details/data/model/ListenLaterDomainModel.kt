package com.allsoftdroid.feature.book_details.data.model

import com.allsoftdroid.database.listenLaterDB.entity.DatabaseListenLaterEntity

internal data class ListenLaterDomainModel(
   val bookId:String,
   val bookName:String,
   val bookAuthor:String,
   val bookDuration:String
)

internal fun ListenLaterDomainModel.toDatabaseModel():DatabaseListenLaterEntity =
    DatabaseListenLaterEntity(
        identifier = bookId,
        title = bookName,
        author = bookAuthor,
        duration = bookDuration,
        timeStamp = System.currentTimeMillis().toString()
    )
