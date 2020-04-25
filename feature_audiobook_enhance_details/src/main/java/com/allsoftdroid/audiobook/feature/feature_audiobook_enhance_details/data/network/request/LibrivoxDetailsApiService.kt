package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request

import org.jsoup.Jsoup

class LibrivoxDetailsApiService : ILibriVoxDetailsApiService {

    override fun getBookDetailsPageAsync(url: String): String? {
        var htmlContent:String?

        Jsoup.connect(url).get().run {
            htmlContent = this.html()
        }

        return htmlContent
    }

}