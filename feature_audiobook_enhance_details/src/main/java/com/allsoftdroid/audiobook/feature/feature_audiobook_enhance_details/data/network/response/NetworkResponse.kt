package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network response received from LibriVox API
 */

@JsonClass(generateAdapter = true)
data class NetworkResponse(
    @field:Json(name = "status") val status:String,
    @field:Json(name = "results") val results : String,
    @field:Json(name = "pagination") val pagination:String,
    @field:Json(name = "search_page") val search_page:String
)