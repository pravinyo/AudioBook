package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network

class Utils{
    object Books{
        private const val BASE_URL = "https://librivox.org/"
        fun getBaseURL() = BASE_URL
        //TODO: Need changes here
        const val param = "title=Poems&author=&reader=&keywords=&genre_id=0&status=all&project_type=either&recorded_language=&sort_order=catalog_date&search_page=1&search_form=advanced&q="
        const val LIBRIVOX = "/advanced_search?$param"
        const val ADVANCED_SEARCH="/advanced_search"
    }
}