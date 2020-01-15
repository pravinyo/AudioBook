package com.allsoftdroid.feature_book.data.network.service

import com.allsoftdroid.feature_book.data.network.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private fun getHttpClientListener():OkHttpClient{
    val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)

    return OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}

/**
 * create retrofit instance
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Utils.Books.getBaseURL())
    .client(getHttpClientListener())
    .build()


/**
 * API to allow access of the following content type as a json string
 */
interface ArchiveLibriVoxAudioBookService{

    @GET(Utils.Books.BOOKS_COLLECTION_PATH) //implement paging
    fun getAudioBooks(@Query(Utils.Books.QUERY_ROW) rowCount:Int, @Query(Utils.Books.QUERY_PAGE) page:Int): Call<String>

    @GET(Utils.Books.LIBREVOX_REPOSITORY_SEARCH)
    fun searchBooks(@Query(Utils.Books.QUERY_SEARCH) search:String,@Query(Utils.Books.QUERY_ROW) rowCount:Int, @Query(Utils.Books.QUERY_PAGE) page:Int): Call<String>
}


/**
 * API is accessible via this object
 */
internal object ArchiveBooksApi{
    val RETROFIT_SERVICE: ArchiveLibriVoxAudioBookService by lazy {
        retrofit.create(ArchiveLibriVoxAudioBookService::class.java)
    }
}