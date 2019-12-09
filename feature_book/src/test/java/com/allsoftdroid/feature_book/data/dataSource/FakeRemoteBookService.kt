package com.allsoftdroid.feature_book.data.dataSource

import com.allsoftdroid.feature_book.data.network.service.ArchiveLibriVoxAudioBookService
import retrofit2.Call

class FakeRemoteBookService : ArchiveLibriVoxAudioBookService {
    override fun getAudioBooks(rowCount: Int, page: Int): Call<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}