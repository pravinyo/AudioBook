package com.allsoftdroid.feature.book_details.utils

import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository

class FakeBookDetailsSharedPref : BookDetailsSharedPreferenceRepository {

    override fun saveTrackPosition(pos: Int) {

    }

    override fun trackPosition(): Int {
        return 0
    }

    override fun saveTrackTitle(title: String) {

    }

    override fun trackTitle(): String {
        return ""
    }

    override fun saveBookId(bookId: String) {

    }

    override fun bookId(): String {
        return ""
    }

    override fun saveBookName(name: String) {

    }

    override fun bookName(): String {
        return ""
    }

    override fun saveTrackFormatIndex(formatIndex: Int) {

    }

    override fun trackFormatIndex(): Int {
      return 0
    }

    override fun clear() {

    }

    override fun saveIsPlaying(isPlaying: Boolean) {

    }

    override fun isPlaying(): Boolean {
        return false
    }
}