package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Success
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

class SearchBookDetailsRepositoryImpl(private val storeCachingRepository:IStoreRepository,
                                      private val bestMatcher: BestBookDetailsParser) :   ISearchBookDetailsRepository {

    /***
     * hold stories related to politics
     */
    private var _listOfItem  = MutableLiveData<List<WebDocument>>()

    private var _bestMatch  = MutableLiveData<List<Pair<Int, WebDocument>>>()

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

                _listOfItem.value = list
                _bestMatch.value = best

                Timber.d("best match is :${_bestMatch.value}")
                Timber.d("Search list is :${_listOfItem.value}")

                listener?.onResponse(Success(true))
            }else{
                Timber.d("Data is empty")
                listener?.onResponse(Failure(Error("No data received")))
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun getSearchBooksList(): LiveData<List<WebDocument>> = _listOfItem

    override fun getBookListWithRanks(bookTitle:String,bookAuthor:String): LiveData<List<Pair<Int, WebDocument>>> {
        return _bestMatch
    }
}