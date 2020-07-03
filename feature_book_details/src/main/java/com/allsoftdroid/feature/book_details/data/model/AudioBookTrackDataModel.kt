package com.allsoftdroid.feature.book_details.data.model

import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import timber.log.Timber

internal data class AudioBookTrackDataModel(
    val name : String,
    val creator : String,
    val title : String,
    val track : String,
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
        trackNumber = getTrackNumber(track),
        length = length,
        format = format,
        size = size
    )

internal fun getTrackNumber(trackNumber:String?):Int{
    trackNumber?.let {
        return if (trackNumber.contains("/")){
            val temp = trackNumber.split("/")
            val track = temp[0].toInt()
            Timber.d("Track(/) Number is :$track")
            track
        }else{
            Timber.d("Track Number is :$trackNumber")
            trackNumber.toInt()
        }
    }?: return 0
}