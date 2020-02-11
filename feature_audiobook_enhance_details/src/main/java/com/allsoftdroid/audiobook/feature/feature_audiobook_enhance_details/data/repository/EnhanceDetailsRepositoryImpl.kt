package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.NetworkResponse
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.EnhanceDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.NetworkResponseListener
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class EnhanceDetailsRepositoryImpl : EnhanceDetailsRepository {

    /***
     * hold stories related to politics
     */
    private lateinit var _bookSearchResult:String

    private var _listOfItem  = MutableLiveData<List<WebDocument>>()

    private var _bookDetails = MutableLiveData<BookDetails>()

    private var _bestMatch  = MutableLiveData<List<Pair<Int, WebDocument>>>()

    /***
     * track network repsonse for  completion and started
     */
    private lateinit var _networkResult:NetworkResult

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }


    override suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int){
        Timber.d("find enhance book details called")

        listener?.onResponse(Loading)
        withContext(Dispatchers.IO){
            Timber.d("Starting network call")
            LibriVoxApi.retrofitService.searchBookInRemoteRepository(
                title = searchTitle,
                author = "",
                search_page = page
            ).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.d("Failure occur")
                    GlobalScope.launch {
                        listener?.onResponse(Failure(Error(t.message)))
                    }
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), NetworkResponse::class.java)
                    Timber.d("Response got:${response.body()}")
                    result?.results?.let {
                        _bookSearchResult = result.results

                        val list = BookBestMatchFromNetworkResult.getList(it)
                        _listOfItem.value = list
                        _bestMatch.value = BookBestMatchFromNetworkResult.getListWithRanks(list,searchTitle,author)

//                        Timber.d("best match is :${_bestMatch.value}")
//                        Timber.d("Search list is :${_listOfItem.value}")
                        Timber.d("Setting response to success")
                        GlobalScope.launch {
                            listener?.onResponse(Success(true))
                        }
                    }
                }
            })
        }
    }

    override fun getSearchBooksList(): LiveData<List<WebDocument>> = _listOfItem

    override fun getBookListWithRanks(bookTitle:String,bookAuthor:String): LiveData<List<Pair<Int, WebDocument>>> {
        return _bestMatch
    }

    override fun networkStatus(): NetworkResult = _networkResult

    override suspend fun fetchBookDetails(bookUrl:String){
        withContext(Dispatchers.IO){
            val details = BookDetailsParsingFromNetworkResponse.loadDetails(bookUrl)
            withContext(Dispatchers.Main){
                _bookDetails.value = details
            }
        }
    }

    override fun getBookDetails(): LiveData<BookDetails> = _bookDetails
}