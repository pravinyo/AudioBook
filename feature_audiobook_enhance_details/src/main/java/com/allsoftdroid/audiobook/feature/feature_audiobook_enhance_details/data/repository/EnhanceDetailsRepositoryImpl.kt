package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.NetworkResponse
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.EnhanceDetailsRepository
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
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
    private lateinit var _bookSearchResult:String

    private lateinit var _listOfItem :List<WebDocument>

    private lateinit var _bookDetails:BookDetails

    lateinit var _bestMatch: List<Pair<Int, WebDocument>>

    /***
     * track network repsonse for  completion and started
     */
    private lateinit var _networkResult:NetworkResult

    override suspend fun searchBookDetailsInRemoteRepository(searchTitle:String,author:String,page:Int){
        Timber.d("find enhance book details called")

        _networkResult = Loading
        withContext(Dispatchers.IO){
            Timber.d("Starting network call")
            LibriVoxApi.retrofitService.searchBookInRemoteRepository(
                title = searchTitle,
                author = author,
                search_page = page
            ).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.d("Failure occur")
                    _networkResult = Failure(Error(t.message))
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), NetworkResponse::class.java)
                    Timber.d("Response got:${response.body()}")
                    result?.results?.let {
                        _networkResult = Success(true)
                        _bookSearchResult = result.results
                        _listOfItem = BookBestMatchFromNetworkResult.getList(it)
                        _bestMatch = BookBestMatchFromNetworkResult.rank
                    }
                }
            })
        }
    }

    override fun getSearchBooksList(): List<WebDocument> = _listOfItem

    override fun getBookListWithRanks(): List<Pair<Int, WebDocument>> = _bestMatch
    override fun networkStatus(): NetworkResult = _networkResult

    override suspend fun fetchBookDetails(bookUrl:String){
        withContext(Dispatchers.IO){
            val details = BookDetailsParsingFromNetworkResponse.loadDetails(bookUrl)
            withContext(Dispatchers.Main){
                _bookDetails = details
            }
        }
    }

    override fun getBookDetails(): BookDetails = _bookDetails
}