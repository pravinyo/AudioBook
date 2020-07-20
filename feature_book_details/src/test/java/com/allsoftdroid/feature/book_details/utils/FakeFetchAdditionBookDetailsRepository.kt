package com.allsoftdroid.feature.book_details.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository

class FakeFetchAdditionBookDetailsRepository : IFetchAdditionBookDetailsRepository {

    private val details = MutableLiveData<BookDetails>()
    private var listener: NetworkResponseListener? = null

    override suspend fun fetchBookDetails(bookUrl: String) {
        details.value = BookDetails(
            null,"1",bookUrl,"url","","","", emptyList()
        )
    }

    fun getBookDetails(): LiveData<BookDetails> {
        return details
    }

    override fun registerNetworkResponse(listener: NetworkResponseListener) {
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }
}