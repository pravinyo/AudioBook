package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network

class Utils{
    object Books{
        private const val BASE_URL = "https://librivox.org/"
        fun getBaseURL() = BASE_URL

        const val ADVANCED_SEARCH="/advanced_search"

        //Param
        const val QUERY_TITLE = "title"
        const val QUERY_AUTHOR = "author"
        const val QUERY_READER = "reader"
        const val QUERY_KEYWORDS = "keywords"
        const val QUERY_GENRE_ID = "genre_id"
        const val QUERY_STATUS = "status"
        const val QUERY_PROJECT_TYPE = "project_type"
        const val QUERY_RECORDED_LANGUAGE = "recorded_language"
        const val QUERY_SORT_ORDER = "sort_order"
        const val QUERY_SEARCH_PAGE = "search_page"
        const val QUERY_SEARCH_FORM= "search_form"
        const val QUERY_Q="q"

        const val DEFAULT_SORT_ORDER = "catalog_date"
        const val DEFAULT_PROJECT_TYPE = "either"
        const val DEFAULT_STATUS = "all"
        const val DEFAULT_SEARCH_FORM = "advanced"
        const val DEFAULT_GENRE_ID = 0
    }
}