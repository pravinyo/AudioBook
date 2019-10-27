package com.allsoftdroid.feature_book.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.NetworkResponseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class GetAudioBookListUsecase( private val audioBookRep: AudioBookRepository) :
    BaseUseCase<GetAudioBookListUsecase.RequestValues,GetAudioBookListUsecase.ResponseValues>(),
        NetworkResponseListener{

    override suspend fun onResponse(result: NetworkResult) {
        withContext(Dispatchers.Main){
            when(result){
                is Success -> useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
                is Failure -> useCaseCallback?.onError(result.error)
            }
        }

        audioBookRep.unRegisterNetworkResponse()
    }

    override suspend fun executeUseCase(requestValues: RequestValues?) {

        Timber.d("Request received data=${requestValues?.pageNumber?:-1}")
        val pageNumber = requestValues?.pageNumber?:1

        audioBookRep.registerNetworkResponse(this)

        audioBookRep.fetchBookList(page = pageNumber)
        Timber.d("fetching started")
    }

    fun getBookList() = audioBookRep.getAudioBooks()

    class RequestValues(val pageNumber : Int) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}