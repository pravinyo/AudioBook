package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class FetchAdditionalBookDetailsUsecase(private val fetchAdditionBookDetailsRepository: IFetchAdditionBookDetailsRepository) :
    BaseUseCase<FetchAdditionalBookDetailsUsecase.RequestValues, FetchAdditionalBookDetailsUsecase.ResponseValues>() {

    public override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {

            fetchAdditionBookDetailsRepository.registerNetworkResponse(listener = object :
                NetworkResponseListener {

                override suspend fun onResponse(result: NetworkResult) {
                    withContext(Dispatchers.Main){
                        when(result){
                            is Success -> useCaseCallback?.onSuccess(ResponseValues(details = "Completed"))
                            is Failure -> useCaseCallback?.onError(result.error)
                            is Loading -> Timber.d("Currently it is loading")
                        }
                    }
                    fetchAdditionBookDetailsRepository.unRegisterNetworkResponse()
                }
            })

            fetchAdditionBookDetailsRepository.fetchBookDetails(bookUrl = it.bookUrl)

        }?:useCaseCallback?.onError(Error("Request is null"))
    }

    fun getAdditionalBookDetails():LiveData<BookDetails>{
        return fetchAdditionBookDetailsRepository.getBookDetails()
    }

    class RequestValues(val bookUrl:String) : BaseUseCase.RequestValues
    class ResponseValues(val details: String) : BaseUseCase.ResponseValues
}