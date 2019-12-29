package com.allsoftdroid.feature_book.data.network

class Utils {
    object Books{
        private const val BASE_URL = "https://archive.org/"
        private const val FILTER_MOST_RECENT = "+AND+mediatype%3A(audio)&fl[]=creator,date,identifier,title&sort[]=-date&output=json"
        private const val QUERY="librivox"
        private const val COLLECTIONS="librivoxaudio"

        const val QUERY_PAGE = "page"
        const val QUERY_ROW = "rows"
        const val QUERY_SEARCH="q"

        const val DEFAULT_ROW_COUNT = 50
        fun getBaseURL() = BASE_URL

        const val BOOKS_COLLECTION_PATH = "/advancedsearch.php?q=($QUERY)+AND+collection%3A($COLLECTIONS)$FILTER_MOST_RECENT"

        const val BOOKS_SEARCH_PATH = "/advancedsearch.php?collection%3A($COLLECTIONS)$FILTER_MOST_RECENT"

    }
}