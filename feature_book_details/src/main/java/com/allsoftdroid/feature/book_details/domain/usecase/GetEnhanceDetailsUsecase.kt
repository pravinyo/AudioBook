package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.EnhanceDetailsRepository
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase

class GetEnhanceDetailsUsecase(private val enhanceDetailsRepository: EnhanceDetailsRepository) :
    BaseUseCase<GetEnhanceDetailsUsecase.RequestValues, GetEnhanceDetailsUsecase.ResponseValues>() {

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        //check network connection
        requestValues?.let {

            enhanceDetailsRepository.searchBookDetailsInRemoteRepository(it.searchTitle,it.author,it.page)

            useCaseCallback?.onSuccess(ResponseValues(Event(true)))
        }?:useCaseCallback?.onError(Error("Request is null"))
    }

    fun getSearchBookList():List<WebDocument>{
        return when(enhanceDetailsRepository.networkStatus()){
            is Success -> enhanceDetailsRepository.getSearchBooksList()
            else -> emptyList()
        }
    }

    fun getBooksWithRanks():List<Pair<Int,WebDocument>>{
        return when(enhanceDetailsRepository.networkStatus()){
            is Success -> enhanceDetailsRepository.getBookListWithRanks()
            else -> emptyList()
        }
    }

    class RequestValues(val searchTitle: String,val author:String="",val page:Int=1) : BaseUseCase.RequestValues
    class ResponseValues(val event: Event<Any>) : BaseUseCase.ResponseValues
}