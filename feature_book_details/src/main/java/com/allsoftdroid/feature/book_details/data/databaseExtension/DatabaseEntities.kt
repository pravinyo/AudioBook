package com.allsoftdroid.feature.book_details.data.databaseExtension

import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel


fun DatabaseMetadataEntity.asMetadataDomainModel():AudioBookMetadataDomainModel =
    AudioBookMetadataDomainModel(
        identifier = identifier,
        creator = creator,
        date = date,
        description = description,
        licenseUrl = licenseUrl,
        tag = tag,
        title = title,
        release_year = release_year,
        runtime = runtime
    )

fun List<DatabaseTrackEntity>.asTrackDomainModel():List<AudioBookTrackDomainModel>{
    return map {
        AudioBookTrackDomainModel(
            filename = it.filename,
            trackTitle = it.trackTitle,
            trackAlbum = it.trackAlbum_id,
            trackNumber = it.trackNumber,
            length = it.length,
            format = it.format,
            size = it.size
        )
    }
}