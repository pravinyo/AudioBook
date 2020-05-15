package com.allsoftdroid.audiobook.feature_listen_later_ui.data.model

import com.allsoftdroid.database.listenLaterDB.entity.DatabaseListenLaterEntity

data class ListenLaterItemDomainModel(
    val identifier:String,
    val title:String,
    val author:String,
    val duration:String,
    val progress:Int=0
)

fun DatabaseListenLaterEntity.toDomainModel() = ListenLaterItemDomainModel(
    identifier = this.identifier,
    title = this.title,
    author = this.author,
    duration = this.duration,
    progress = 0
)