package com.allsoftdroid.feature_book.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.NetworkResponseListener

class FakeAudioBookRepository(private val manualFailure:Boolean = false) : AudioBookRepository{
    private var listener:NetworkResponseListener? = null

    companion object{
        const val FAILURE_MESSAGE="Manual Failure triggered"
    }

    override fun registerNetworkResponse(listener: NetworkResponseListener) {
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        listener = null
    }

    private var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()

    override suspend fun fetchBookList(page: Int) {
        if(!manualFailure){
            val list = ArrayList<AudioBookDomainModel>()
            list.add(AudioBookDomainModel("1","Title","creator","2019"))

            audioBooks.value = list

            listener?.onResponse(Success(result = list.size))
        }else{
            audioBooks.value = emptyList()

            listener?.onResponse(Failure(error = Error(FAILURE_MESSAGE)))
        }
    }

    override fun getAudioBooks()= audioBooks

    override suspend fun searchBookList(query: String, page: Int) {

    }

    override fun getSearchBooks(): LiveData<List<AudioBookDomainModel>> {
        val list = MutableLiveData<List<AudioBookDomainModel>>()
        return list
    }
}