package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.ILibriVoxApiService
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.ILibriVoxDetailsApiService
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.LibrivoxDetailsApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


class LibriVoxApi{

    companion object{
        /**
         * API is accessible via this object
         */
        val retrofitService:ILibriVoxApiService by lazy {
            retrofit.create(ILibriVoxApiService::class.java)
        }

        val bookDetailsApiService:ILibriVoxDetailsApiService by lazy {
            LibrivoxDetailsApiService()
        }

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
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(Utils.Books.getBaseURL())
            .client(getHttpClientListener())
            .build()

    }
}

