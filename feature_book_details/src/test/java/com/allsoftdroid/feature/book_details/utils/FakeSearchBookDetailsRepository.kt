package com.allsoftdroid.feature.book_details.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.common.base.network.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeSearchBookDetailsRepository : ISearchBookDetailsRepository {

    private var listener: NetworkResponseListener? = null
    private lateinit var bookList : List<WebDocument>

    override suspend fun searchBookDetailsInRemoteRepository(
        searchTitle: String,
        author: String,
        page: Int
    ) {
        val doc = WebDocument("The Art of War","pravin","None", mutableListOf("novel"))
        bookList = mutableListOf(doc)

        listener?.onResponse(Success(result = "success"))
    }

    override fun getSearchBooksList(): Flow<List<WebDocument>> {
        return flow { emit(bookList) }
    }

    override fun getBookListWithRanks(
        bookTitle: String,
        bookAuthor: String
    ): Flow<List<Pair<Int, WebDocument>>> {
        return flow { emit(mutableListOf(Pair(3,bookList[0]))) }
    }

    override fun registerNetworkResponse(listener: NetworkResponseListener) {
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }
}