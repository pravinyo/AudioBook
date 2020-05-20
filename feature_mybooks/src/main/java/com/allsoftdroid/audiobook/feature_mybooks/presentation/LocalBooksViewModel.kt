package com.allsoftdroid.audiobook.feature_mybooks.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import com.allsoftdroid.audiobook.feature_mybooks.di.LocalBooksModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_mybooks.di.LocalBooksModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature_mybooks.domain.LocalBookListUsecase
import com.allsoftdroid.audiobook.feature_mybooks.utils.Empty
import com.allsoftdroid.audiobook.feature_mybooks.utils.RequestStatus
import com.allsoftdroid.audiobook.feature_mybooks.utils.Started
import com.allsoftdroid.audiobook.feature_mybooks.utils.Success
import com.allsoftdroid.common.base.extension.Event
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class LocalBooksViewModel(
    private val bookListUsecase: LocalBookListUsecase
) : ViewModel(),KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var _books:List<LocalBookDomainModel>?  = null

    private var _requestStatus = MutableLiveData<Event<RequestStatus>>()
    val requestStatus : LiveData<Event<RequestStatus>> = _requestStatus


    private fun loadBooks(){
        viewModelScope.launch {
            Timber.d("sending started response")
            _requestStatus.value = Event(Started)

            val books = bookListUsecase.getBookList()
            if(books.isEmpty()){
                Timber.d("books is empty sending empty response")
                _requestStatus.value = Event(Empty)
            }else{
                Timber.d("books is not empty sending response")
                _requestStatus.value = Event(Success(books))
                _books = books
            }
        }
    }

    fun loadFromCacheOrReload(){

        val books = _books

        if (books.isNullOrEmpty()){
            loadBooks()
        }else{
            _requestStatus.value = Event(Success(books))
        }
    }

    fun removeBook(identifier:String){
        viewModelScope.launch {
            bookListUsecase.removeBook(identifier)
            loadBooks()
        }
    }

    fun removeAllChapters(identifier: String){
        viewModelScope.launch {
            bookListUsecase.removeChapters(identifier)
            loadBooks()
        }
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}