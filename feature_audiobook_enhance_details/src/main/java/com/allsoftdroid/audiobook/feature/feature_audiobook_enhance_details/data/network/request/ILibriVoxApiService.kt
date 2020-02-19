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
        @Query(Utils.Books.QUERY_TITLE) title:String,
        @Query(Utils.Books.QUERY_AUTHOR) author:String="",
        @Query(Utils.Books.QUERY_READER) reader:String="",
        @Query(Utils.Books.QUERY_KEYWORDS) keywords:String="",
        @Query(Utils.Books.QUERY_GENRE_ID) genre_id:Int= Utils.Books.DEFAULT_GENRE_ID,
        @Query(Utils.Books.QUERY_STATUS) status:String= Utils.Books.DEFAULT_STATUS,
        @Query(Utils.Books.QUERY_PROJECT_TYPE) project_type:String= Utils.Books.DEFAULT_PROJECT_TYPE,
        @Query(Utils.Books.QUERY_RECORDED_LANGUAGE) recorded_language:String="",
        @Query(Utils.Books.QUERY_SORT_ORDER) sort_order:String = Utils.Books.DEFAULT_SORT_ORDER,
        @Query(Utils.Books.QUERY_SEARCH_PAGE) search_page:Int=1,
        @Query(Utils.Books.QUERY_SEARCH_FORM) search_form:String= Utils.Books.DEFAULT_SEARCH_FORM,
        @Query(Utils.Books.QUERY_Q) q:String=""): Deferred<Response<NetworkResponse>>
}