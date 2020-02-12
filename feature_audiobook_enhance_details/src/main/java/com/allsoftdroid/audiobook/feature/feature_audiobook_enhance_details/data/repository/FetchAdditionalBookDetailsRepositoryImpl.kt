package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchAdditionalBookDetailsRepositoryImpl :
    IFetchAdditionBookDetailsRepository {


    private var _bookDetails = MutableLiveData<BookDetails>()

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }

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