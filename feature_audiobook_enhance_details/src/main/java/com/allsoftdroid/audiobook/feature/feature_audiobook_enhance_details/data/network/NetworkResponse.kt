package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network

/**
 * Network response received from LibriVox API
 */
data class NetworkResponse(
    val status:String,
    val results : String,
    val pagination:String,
    val search_page:String
)