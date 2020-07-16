package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.utils.NetworkState
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class GetMetadataUsecase( private val metadataRepository: IMetadataRepository) :
    BaseUseCase<GetMetadataUsecase.RequestValues, GetMetadataUsecase.ResponseValues>(){

    private var disposable: CompositeDisposable = CompositeDisposable()

    override suspend fun executeUseCase(requestValues: RequestValues?) {

        metadataRepository.loadMetadata()
        Timber.d("fetching started")

        disposable.add(metadataRepository.networkResponse().observable.subscribe {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){

                    NetworkState.SERVER_ERROR,
                    NetworkState.CONNECTION_ERROR -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            useCaseCallback?.onError(Error(networkState.value))
                        }
                    }

                    NetworkState.COMPLETED->{
                        GlobalScope.launch(Dispatchers.Main) {
                            val responseValues = ResponseValues(Event(networkState.value))
                            useCaseCallback?.onSuccess(responseValues)
                        }
                    }

                    NetworkState.LOADING -> {Timber.d("Loading from repository")}
                }
            }
        })
    }

    fun getMetadata() = metadataRepository.getMetadata()

    fun getBookIdentifier() = metadataRepository.getBookId()

    fun dispose(){
        metadataRepository.cancelRequestInFlight()
        disposable.dispose()
    }

    class RequestValues(val bookId : String) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}