package com.allsoftdroid.feature.book_details.data.model

import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity

internal data class AudioBookTrackDataModel(
    val name : String,
    val creator : String,
    val title : String,
    val track : Int,
    val album : String,
    val genre : String,
    val length: String,
    val format: String,
    val size : String
)

internal fun AudioBookTrackDataModel.toDatabaseModel(id:String):DatabaseTrackEntity =
    DatabaseTrackEntity(
        track_id = "$id@$track@$format",
        trackAlbum_id = id,
        filename = name,
        trackTitle = title,
        trackNumber = track,
        length = length,
        format = format,
        size = size
    )