package com.allsoftdroid.feature_book.data.network

class Utils {
    object Books{
        private const val BASE_URL = "https://archive.org/"
        private const val FILTER_MOST_RECENT = "+AND+mediatype%3A(audio)&fl[]=description,identifier,title&sort[]=-date&rows=50&output=json&page=1"
        private const val QUERY="librivox"
        private const val COLLECTIONS="librivoxaudio"

        fun getBaseURL() = BASE_URL

        const val BOOKS_COLLECTION = "/advancedsearch.php?q=($QUERY)+AND+collection%3A($COLLECTIONS)$FILTER_MOST_RECENT"
    }
}