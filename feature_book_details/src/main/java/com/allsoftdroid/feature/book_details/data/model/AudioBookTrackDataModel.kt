package com.allsoftdroid.feature.book_details.data.model

import android.os.Build
import android.text.Html
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
        trackTitle = getTrackTitle(title),
        trackNumber = getTrackNumber(track),
        length = length,
        format = format,
        size = size
    )

fun getTrackTitle(title: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        Html.fromHtml(title,Html.FROM_HTML_MODE_LEGACY).toString()
    }else{
        Html.fromHtml(title).toString()
    }
}

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