package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


private fun getHttpClientListener(): OkHttpClient {
    val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)

    return OkHttpClient.Builder()
        .addInterceptor(logging)
        .readTimeout(30,TimeUnit.SECONDS)
        .connectTimeout(30,TimeUnit.SECONDS)
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
interface LibriVoxApiService{

//    @Headers("X-Requested-With:XMLHttpRequest")
//    @GET(Utils.Books.LIBRIVOX)
//    fun getBooks():Call<String>

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET(Utils.Books.ADVANCED_SEARCH)
    fun searchBookInRemoteRepository(
        @Query("title") title:String,
        @Query("author") author:String="",
        @Query("reader") reader:String="",
        @Query("keywords") keywords:String="",
        @Query("genre_id") genre_id:Int=0,
        @Query("status") status:String="all",
        @Query("project_type") project_type:String="either",
        @Query("recorded_language") recorded_language:String="",
        @Query("sort_order") sort_order:String="catalog_date",
        @Query("search_page") search_page:Int=1,
        @Query("search_form") search_form:String="advanced",
        @Query("q") q:String=""):Call<String>
}


/**
 * API is accessible via this object
 */
object LibriVoxApi{
    val retrofitService:LibriVoxApiService by lazy {
        retrofit.create(LibriVoxApiService::class.java)
    }
}