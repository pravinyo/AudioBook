package com.allsoftdroid.audiobook.feature_mybooks.data.model

data class LocalBookDomainModel (
    val bookTitle:String,
    val bookIdentifier:String,
    val bookAuthor:String,
    val bookChaptersDownloaded:Int,
    val totalChapters:Int,
    val fileNames:List<String>
)