package com.allsoftdroid.feature.book_details.utils

enum class NetworkState(val value:String) {
    CONNECTION_ERROR("Network Error"),
    SERVER_ERROR("Server Error"),
    LOADING("Loading"),
    COMPLETED("Done")
}