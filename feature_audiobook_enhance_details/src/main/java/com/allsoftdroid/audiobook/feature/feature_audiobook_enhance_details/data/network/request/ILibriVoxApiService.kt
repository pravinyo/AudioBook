package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.response.NetworkResponse
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.Utils
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


/**
 * API to allow access of the following content type as a Deferred NetworkResponse
 */


interface ILibriVoxApiService {

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET(Utils.Books.ADVANCED_SEARCH)
    fun searchBookInRemoteRepositoryAsync(
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
        @Query("q") q:String=""): Deferred<Response<NetworkResponse>>
}