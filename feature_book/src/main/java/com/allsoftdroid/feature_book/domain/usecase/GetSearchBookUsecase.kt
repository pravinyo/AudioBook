package com.allsoftdroid.feature_book.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.repository.NetworkResponseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GetSearchBookUsecase( private val audioBookRep: AudioBookRepository) :
    BaseUseCase<GetSearchBookUsecase.RequestValues, GetSearchBookUsecase.ResponseValues>(){

    public override suspend fun executeUseCase(requestValues: RequestValues?) {

        Timber.d("Request received data=${requestValues?.pageNumber?:-1} and query=${requestValues?.query}")
        val pageNumber = requestValues?.pageNumber?:1
        val query = requestValues?.query?:""

        audioBookRep.registerNetworkResponse(listener = object : NetworkResponseListener {

            override suspend fun onResponse(result: NetworkResult) {
                withContext(Dispatchers.Main){
                    when(result){
                        is Success -> useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
                        is Failure -> useCaseCallback?.onError(result.error)
                        is Loading -> Timber.d("Loading")
                    }
                }

                audioBookRep.unRegisterNetworkResponse()
            }
        })

        if(query.isNotEmpty()){
            audioBookRep.searchBookList(query = query,page = pageNumber)
            Timber.d("fetching started")
        }else{
            useCaseCallback?.onError(Error("Query is Empty"))
            Timber.d("fetching ERROR")
        }

    }

    fun getSearchResults() = audioBookRep.getSearchBooks()

    fun cancelRequestInFlight() = audioBookRep.cancelRequestInFlight()

    class RequestValues(val query: String, val pageNumber : Int) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}