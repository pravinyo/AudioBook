package com.allsoftdroid.feature.book_details.domain.model

data class AudioBookMetadataDomainModel(
    val identifier : String,
    val creator : String,
    val date : String,
    val description : String,
    val licenseUrl : String,
    val tag : String,
    val title : String,
    val release_year : String,
    val runtime: String
)