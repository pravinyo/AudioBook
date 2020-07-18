package com.allsoftdroid.feature_book.data.repository

import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.NetworkResponseListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    override fun cancelRequestInFlight() {

    }

    private var audioBooks:List<AudioBookDomainModel> = emptyList()

    override suspend fun fetchBookList(page: Int) {
        if(!manualFailure){
            val list = ArrayList<AudioBookDomainModel>()
            list.add(AudioBookDomainModel("1","Title","creator","2019","2020-06-01T11:22:00"))

            audioBooks = list

            listener?.onResponse(Success(result = list.size))
        }else{
            audioBooks = emptyList()

            listener?.onResponse(Failure(error = Error(FAILURE_MESSAGE)))
        }
    }

    override fun getAudioBooks(): Flow<List<AudioBookDomainModel>> = flow { emit(audioBooks) }

    override suspend fun searchBookList(query: String, page: Int) {

    }

    override fun getSearchBooks(): Flow<List<AudioBookDomainModel>> {
        return flow { emit(emptyList()) }
    }
}