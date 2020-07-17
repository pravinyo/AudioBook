package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.allsoftdroid.common.base.network.Failure
import com.allsoftdroid.common.base.network.Success
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class FetchAdditionalBookDetailsRepositoryImpl(private val storeCachingRepository: IStoreRepository,
                                               private val bookDetailsParser: BookDetailsParserFromHtml
) :
    IFetchAdditionBookDetailsRepository {

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }

    override suspend fun fetchBookDetails(bookUrl:String){
        withContext(Dispatchers.IO){
            var details:BookDetails?=null

            try {
                val pageData = storeCachingRepository.provideEnhanceBookDetailsStore().get(bookUrl)

                if(pageData.isNotEmpty()){
                    details = bookDetailsParser.loadDetails(pageData)
                    Timber.d("Details:$details")
                }

                listener?.onResponse(Success(details))

            }catch (exception:Exception){
                Timber.e("Found Exception:$exception")
                listener?.onResponse(Failure(Error(exception.message)))
            }
        }
    }
}