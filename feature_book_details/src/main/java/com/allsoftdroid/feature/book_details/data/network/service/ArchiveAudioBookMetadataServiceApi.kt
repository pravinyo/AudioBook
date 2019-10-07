package com.allsoftdroid.feature.book_details.data.network.service

import com.allsoftdroid.feature.book_details.data.network.Utils
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * create retrofit instance
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Utils.MetaData.getBaseURL())
    .build()


/**
 * API to allow access of the following content type as a json string
 */
internal interface ArchiveMetadataService{

    @GET("${Utils.MetaData.PATH}{book_id}")
    fun getMetadata(@Path("book_id") bookId:String): Call<String>
}



/**
 * API is accessible via this object
 */
internal object ArchiveMetadataApi{
    val RETROFIT_SERVICE: ArchiveMetadataService by lazy {
        retrofit.create(ArchiveMetadataService::class.java)
    }
}