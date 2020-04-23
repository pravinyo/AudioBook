package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request

interface ILibriVoxDetailsApiService {
    fun getBookDetailsPageAsync(url:String):String?
}