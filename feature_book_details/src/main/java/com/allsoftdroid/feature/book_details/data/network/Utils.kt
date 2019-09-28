package com.allsoftdroid.feature.book_details.data.network

class Utils {
    object MetaData{
        private const val BASE_URL = "https://archive.org/"
        fun getBaseURL() = BASE_URL

        const val PATH = "/metadata/"
    }
}