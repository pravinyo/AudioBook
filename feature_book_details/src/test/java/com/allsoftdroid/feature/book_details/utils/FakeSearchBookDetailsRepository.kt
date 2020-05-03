package com.allsoftdroid.feature.book_details.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.common.base.network.Success

class FakeSearchBookDetailsRepository : ISearchBookDetailsRepository {

    private var listener: NetworkResponseListener? = null
    private val bookList = MutableLiveData<List<WebDocument>>()

    override suspend fun searchBookDetailsInRemoteRepository(
        searchTitle: String,
        author: String,
        page: Int
    ) {
        val doc = WebDocument("The Art of War","pravin","None", mutableListOf("novel"))
        bookList.value = mutableListOf(doc)

        listener?.onResponse(Success(result = "success"))
    }

    override fun getSearchBooksList(): LiveData<List<WebDocument>> {
        return bookList
    }

    override fun getBookListWithRanks(
        bookTitle: String,
        bookAuthor: String
    ): LiveData<List<Pair<Int, WebDocument>>> {
        val rankList = MutableLiveData<List<Pair<Int, WebDocument>>>()
        rankList.value = mutableListOf(Pair(3,bookList.value!![0]))

        return rankList
    }

    override fun registerNetworkResponse(listener: NetworkResponseListener) {
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }
}