package com.allsoftdroid.feature_book.data.network.service

import com.allsoftdroid.feature_book.data.network.Utils
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

/**
 * create retrofit instance
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Utils.Books.getBaseURL())
    .build()


/**
 * API to allow access of the following content type as a json string
 */
internal interface ArchiveLibriVoxAudioBookService{

    @GET(Utils.Books.BOOKS_COLLECTION_PATH) //implement paging
    fun getAudioBooks(): Call<String>
}


/**
 * API is accessible via this object
 */
internal object ArchiveBooksApi{
    val RETROFIT_SERVICE: ArchiveLibriVoxAudioBookService by lazy {
        retrofit.create(ArchiveLibriVoxAudioBookService::class.java)
    }
}