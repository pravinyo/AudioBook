package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository

class FakeBookDetailsRepository: IFetchAdditionBookDetailsRepository {

    private val details = MutableLiveData<BookDetails>()

    override suspend fun fetchBookDetails(bookUrl: String) {
        details.value = BookDetails(
            null,"1",bookUrl,"url","","","", emptyList()
        )
    }

    override fun getBookDetails(): LiveData<BookDetails> {
        return details
    }

    override fun registerNetworkResponse(listener: NetworkResponseListener) {

    }

    override fun unRegisterNetworkResponse() {

    }
}