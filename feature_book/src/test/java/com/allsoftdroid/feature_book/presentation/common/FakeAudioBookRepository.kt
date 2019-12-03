package com.allsoftdroid.feature_book.presentation.common

import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.NetworkResponseListener

class FakeAudioBookRepository : AudioBookRepository{
    override fun registerNetworkResponse(listener: NetworkResponseListener) {

    }

    override fun unRegisterNetworkResponse() {

    }

    private var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()

    override suspend fun fetchBookList(page: Int) {
        val list = ArrayList<AudioBookDomainModel>()
        list.add(AudioBookDomainModel("1","Title","creator","2019"))

        audioBooks.value = list
    }

    override fun getAudioBooks()= audioBooks
}