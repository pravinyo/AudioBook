package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BestBookDetailsParser
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Success
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

class SearchBookDetailsRepositoryImpl(private val storeCachingRepository:IStoreRepository,
                                      private val bestMatcher: BestBookDetailsParser
) :   ISearchBookDetailsRepository {

    /***
     * hold books result
     */
    private lateinit var  _listOfItem:Flow<List<WebDocument>>

    private lateinit var _bestMatch:Flow<List<Pair<Int, WebDocument>>>

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }


    @ExperimentalCoroutinesApi
    @FlowPreview
    override suspend fun searchBookDetailsInRemoteRepository(searchTitle:String, author:String, page:Int){

        try{
            val data = storeCachingRepository.provideEnhanceBookSearchStore().get(Pair(searchTitle,page))

            if(data.isNotEmpty()){
                val list = bestMatcher.getList(data)
                val best = bestMatcher.getListWithRanks(list,searchTitle,author)

                _listOfItem = flow{ emit(list) }
                _bestMatch = flow { emit(best) }

                Timber.d("best match is :$best")
                Timber.d("Search list is :$list")

                listener?.onResponse(Success(true))
            }else{
                _listOfItem = flowOf(emptyList())
                _bestMatch = flowOf(emptyList())
                Timber.d("Data is empty")
                listener?.onResponse(Failure(Error("No data received")))
            }
        }catch (e:Exception){
            _listOfItem = flowOf(emptyList())
            _bestMatch = flowOf(emptyList())
            e.printStackTrace()
        }
    }

    override fun getSearchBooksList(): Flow<List<WebDocument>> = _listOfItem

    override fun getBookListWithRanks(bookTitle:String,bookAuthor:String): Flow<List<Pair<Int, WebDocument>>> {
        return _bestMatch
    }
}