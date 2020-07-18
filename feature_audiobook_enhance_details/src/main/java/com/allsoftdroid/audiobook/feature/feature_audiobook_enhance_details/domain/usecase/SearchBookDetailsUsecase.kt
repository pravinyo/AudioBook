package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class SearchBookDetailsUsecase(private val searchBookDetailsRepository: ISearchBookDetailsRepository) :
    BaseUseCase<SearchBookDetailsUsecase.RequestValues, SearchBookDetailsUsecase.ResponseValues>() {

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {

            searchBookDetailsRepository.registerNetworkResponse(listener = object :
                NetworkResponseListener {

                override suspend fun onResponse(result: NetworkResult) {
                    withContext(Dispatchers.Main){
                        when(result){
                            is Success -> useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
                            is Failure -> useCaseCallback?.onError(result.error)
                            is Loading -> Timber.d("Currently it is loading")
                        }
                    }
                    searchBookDetailsRepository.unRegisterNetworkResponse()
                }
            })

            searchBookDetailsRepository.searchBookDetailsInRemoteRepository(searchTitle = it.searchTitle,page = it.page,author = it.author)

        }?:useCaseCallback?.onError(Error("Request is null"))
    }

    fun getSearchBookList() = searchBookDetailsRepository.getSearchBooksList()

    fun getBooksWithRanks(bookTitle:String,bookAuthor:String):Flow<List<Pair<Int,WebDocument>>>{
        return searchBookDetailsRepository.getBookListWithRanks(bookTitle,bookAuthor)
    }

    class RequestValues(val searchTitle: String,val author:String="",val page:Int=1) : BaseUseCase.RequestValues
    class ResponseValues(val list: Event<Any>) : BaseUseCase.ResponseValues
}