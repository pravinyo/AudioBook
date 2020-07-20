package com.allsoftdroid.audiobook.utils

import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository

class FakeBookDetailsSharedPref : BookDetailsSharedPreferenceRepository {

    private var trackPos = 0
    private var trackTitle = ""
    private var bookId = ""
    private var bookname = ""
    private var trackFormat = 0
    private var isPlaying = false

    override fun saveTrackPosition(pos: Int) {
        trackPos = pos
    }

    override fun trackPosition(): Int {
        return trackPos
    }

    override fun saveTrackTitle(title: String) {
        trackTitle = title
    }

    override fun trackTitle(): String {
        return trackTitle
    }

    override fun saveBookId(id: String) {
        bookId = id
    }

    override fun bookId(): String {
        return bookId
    }

    override fun saveBookName(name: String) {
        bookname = name
    }

    override fun bookName(): String {
        return bookname
    }

    override fun saveTrackFormatIndex(formatIndex: Int) {
        trackFormat = formatIndex
    }

    override fun trackFormatIndex(): Int {
        return trackFormat
    }

    override fun clear() {
        trackTitle = ""
        trackPos = 0
        bookId = ""
        bookname = ""
        trackFormat = 0
        isPlaying = false
    }

    override fun saveIsPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }

    override fun isToolTipShown(): Boolean {
        return true
    }

    override fun setToolTipShown(shouldSkip: Boolean) {

    }
}