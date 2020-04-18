package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model

data class BookDetails(
    var webDocument: WebDocument? = null,
    val runtime:String,
    val archiveUrl : String,
    val gutenbergUrl:String,
    val description:String,
    val genres:String,
    val language:String,
    val chapters : List<Chapter>
)