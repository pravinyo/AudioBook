package com.allsoftdroid.feature.book_details.data.model

import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity

internal data class AudioBookTrackDataModel(
    val filename : String,
    val creator : String,
    val trackTitle : String,
    val trackNumber : Int,
    val trackAlbum : String,
    val genre : String,
    val length: String,
    val format: String,
    val size : String
)

internal fun AudioBookTrackDataModel.toDatabaseModel(id:String):DatabaseTrackEntity =
    DatabaseTrackEntity(
        track_id = "$id@$trackNumber@$format",
        trackAlbum_id = id,
        filename = filename?:"NULL",
        trackTitle = trackTitle,
        trackNumber = trackNumber,
        length = length,
        format = format,
        size = size
    )