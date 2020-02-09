package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.NetworkResponse
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.EnhanceDetailsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class EnhanceDetailsRepositoryImpl : EnhanceDetailsRepository {

    /***
     * hold stories related to politics
     */
    private var _bookSearchResult = MutableLiveData<String>()
    val bookSearchResult:LiveData<String> = _bookSearchResult

    private var _listOfItem = MutableLiveData<List<WebDocument>>()
    val listOfItem : LiveData<List<WebDocument>> = _listOfItem

    private var _bookDetails = MutableLiveData<BookDetails>()
    val bookDetails:LiveData<BookDetails> = _bookDetails

    lateinit var bestMatch: List<Pair<Int, WebDocument>>

    /***
     * track network repsonse for  completion and started
     */
    private var _response = MutableLiveData<Int>()
    val response: LiveData<Int>
        get() = _response


    override suspend fun searchBookDetails(){
        Timber.d("find enhance book details called")

        _response.value = null
        withContext(Dispatchers.IO){
            Timber.d("Starting network call")
            LibriVoxApi.retrofitService.getBooks().enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.d("Failure occur")
                    _response.value = null
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), NetworkResponse::class.java)
                    Timber.d("Response got:${response.body()}")
                    result?.results?.let {
                        _response.value = result.status.length
                        _bookSearchResult.value = result.results
                        _listOfItem.value = BookBestMatchFromNetworkResultRepository.getList(it)
                        bestMatch = BookBestMatchFromNetworkResultRepository.rank
                    }
                }
            })
        }
    }

    override suspend fun fetchBookDetails(bookUrl:String){
        withContext(Dispatchers.IO){
            val details = BookDetailsParsingFromNetworkResponseRespository.loadDetails(bookUrl)
            withContext(Dispatchers.Main){
                _bookDetails.value = details
            }
        }
    }
}