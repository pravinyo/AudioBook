package com.allsoftdroid.feature.book_details.data.model


internal data class AudioBookMetadataDataModel(
    val identifier : String,
    val creator : String,
    val date : String,
    val description : String,
    val licenseurl : String,
    val subject : String,
    val title : String,
    val publicdate : String,
    val runtime: String?
)