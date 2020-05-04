package com.allsoftdroid.feature.book_details.utils

import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataService
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FakeRemoteMetadataSource: ArchiveMetadataService {
    override fun getMetadata(bookId: String): Call<String> {
        return object : Call<String>{
            override fun enqueue(callback: Callback<String>) {

            }

            override fun isExecuted(): Boolean {
                return true
            }

            override fun clone(): Call<String> {
                return this
            }

            override fun isCanceled(): Boolean {
                return false
            }

            override fun cancel() {

            }

            override fun execute(): Response<String> {
                return Response.success("")
            }

            override fun request(): Request {
                return Request.Builder().build()
            }
        }
    }
}