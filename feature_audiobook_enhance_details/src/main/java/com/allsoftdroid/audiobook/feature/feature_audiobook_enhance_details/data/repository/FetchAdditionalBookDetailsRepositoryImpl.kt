package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class FetchAdditionalBookDetailsRepositoryImpl(private val storeCachingRepository: IStoreRepository,
                                               private val bookDetailsParser: BookDetailsParserFromHtml) :
    IFetchAdditionBookDetailsRepository {

    private var _bookDetails = MutableLiveData<BookDetails>()

    private var listener: NetworkResponseListener? = null

    override fun registerNetworkResponse(listener: NetworkResponseListener){
        this.listener = listener
    }

    override fun unRegisterNetworkResponse() {
        this.listener = null
    }

    override suspend fun fetchBookDetails(bookUrl:String){
        withContext(Dispatchers.IO){
            var error=""
            var details:BookDetails?=null

            try {
                val pageData = storeCachingRepository.provideEnhanceBookDetailsStore().get(bookUrl)

                if(pageData.isNotEmpty()){
                    details = bookDetailsParser.loadDetails(pageData)
                }
            }catch (exception:Exception){
                exception.printStackTrace()
                error = exception.toString()
            }

            withContext(Dispatchers.Main){
                _bookDetails.value = details
                if(error.isNotEmpty()){
                    Timber.e("Found Exception:$error")
                }
            }
        }
    }

    override fun getBookDetails(): LiveData<BookDetails> = _bookDetails
}