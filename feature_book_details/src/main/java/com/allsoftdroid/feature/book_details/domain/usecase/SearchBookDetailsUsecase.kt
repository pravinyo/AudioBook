package com.allsoftdroid.feature.book_details.domain.usecase

import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.NetworkResponseListener
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Loading
import com.allsoftdroid.common.base.network.NetworkResult
import com.allsoftdroid.common.base.network.Success
import com.allsoftdroid.common.base.usecase.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SearchBookDetailsUsecase(private val ISearchBookDetailsRepository: ISearchBookDetailsRepository) :
    BaseUseCase<SearchBookDetailsUsecase.RequestValues, SearchBookDetailsUsecase.ResponseValues>() {

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {

            ISearchBookDetailsRepository.registerNetworkResponse(listener = object : NetworkResponseListener {

                override suspend fun onResponse(result: NetworkResult) {
                    withContext(Dispatchers.Main){
                        when(result){
                            is Success -> useCaseCallback?.onSuccess(ResponseValues(Event(true)))
                            is Failure -> useCaseCallback?.onError(result.error)
                            is Loading -> Timber.d("Currently it is loading")
                        }
                    }
                    ISearchBookDetailsRepository.unRegisterNetworkResponse()
                }
            })

            ISearchBookDetailsRepository.searchBookDetailsInRemoteRepository(searchTitle = it.searchTitle,page = it.page,author = it.author)

        }?:useCaseCallback?.onError(Error("Request is null"))
    }

    fun getSearchBookList():LiveData<List<WebDocument>>{
        return ISearchBookDetailsRepository.getSearchBooksList()
    }

    fun getBooksWithRanks(bookTitle:String,bookAuthor:String):LiveData<List<Pair<Int,WebDocument>>>{
        return ISearchBookDetailsRepository.getBookListWithRanks(bookTitle,bookAuthor)
    }

    class RequestValues(val searchTitle: String,val author:String="",val page:Int=1) : BaseUseCase.RequestValues
    class ResponseValues(val list: Event<Any>) : BaseUseCase.ResponseValues
}