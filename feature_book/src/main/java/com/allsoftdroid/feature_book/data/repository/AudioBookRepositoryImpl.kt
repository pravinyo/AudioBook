package com.allsoftdroid.feature_book.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.data.model.toDomainModel
import com.allsoftdroid.feature_book.data.network.response.GetAudioBooksResponse
import com.allsoftdroid.feature_book.data.network.service.ArchiveBooksApi
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class AudioBookRepositoryImpl : AudioBookRepository {

    var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()

    override suspend fun searchAudioBooks() {

        withContext(Dispatchers.IO){
            Log.i(AudioBookRepositoryImpl::class.java.name,"Starting network call")

            ArchiveBooksApi.RETROFIT_SERVICE.getAudioBooks().enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.i(AudioBookRepositoryImpl::class.java.simpleName,"Failure occur")
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), GetAudioBooksResponse::class.java)

                    Log.i(AudioBookRepositoryImpl::class.java.simpleName,"Response got: ${result.response.docs[0].title}")

                    result?.response?.docs?.let {
                        audioBooks.value = it.map { it.toDomainModel() }
                    }
                }
            })
        }
    }

    override suspend fun getAudioBooks() = audioBooks
}