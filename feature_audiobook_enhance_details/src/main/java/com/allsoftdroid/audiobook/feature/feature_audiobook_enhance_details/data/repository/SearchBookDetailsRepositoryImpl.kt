package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.NetworkResponse
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.common.base.network.Failure
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

class SearchBookDetailsRepositoryImpl :   ISearchBookDetailsRepository {

    /***
     * hold stories related to politics
     */
    private lateinit var _bookSearchResult:String

    private var _listOfItem  = MutableLiveData<List<WebDocument>>()

    private var _bestMatch  = MutableLiveData<List<Pair<Int, WebDocument>>>()

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }


    override suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int){
        Timber.d("find enhance book details called")

//        listener?.onResponse(Loading)
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
                        Timber.d("Setting response to success")
                        GlobalScope.launch {
                            val list = BookBestMatchFromNetworkResult.getList(it)
                            val best = BookBestMatchFromNetworkResult.getListWithRanks(list,searchTitle,author)
                            withContext(Dispatchers.Main){

                                _listOfItem.value = list
                                _bestMatch.value = best

                                Timber.d("best match is :${_bestMatch.value}")
                                Timber.d("Search list is :${_listOfItem.value}")
                            }

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
}