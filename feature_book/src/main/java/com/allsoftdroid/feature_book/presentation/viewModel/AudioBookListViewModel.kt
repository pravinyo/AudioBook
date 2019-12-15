package com.allsoftdroid.feature_book.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.di.FeatureBookModule.SUPER_VISOR_JOB
import com.allsoftdroid.feature_book.di.FeatureBookModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.utils.NetworkState
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class AudioBookListViewModel(
    private val useCaseHandler : UseCaseHandler,
    private val getAlbumListUseCase:GetAudioBookListUsecase) : ViewModel(),KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob:CompletableJob  by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope :CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    //track network response
    private val _networkResponse = MutableLiveData<Event<NetworkState>>()
    val networkResponse : LiveData<Event<NetworkState>>
    get() = _networkResponse


    //handle item click event
    private var _itemClicked = MutableLiveData<Event<String>>()
    val itemClicked: LiveData<Event<String>>
        get() = _itemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    private var requestValues  = GetAudioBookListUsecase.RequestValues(pageNumber = 1)

    //book list state change event
    private val listChangedEvent = MutableLiveData<Event<Any>>()

    //audio book list reference
    val audioBooks:LiveData<List<AudioBookDomainModel>> = Transformations.switchMap(listChangedEvent){
        getAlbumListUseCase.getBookList()
    }

    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            if(audioBooks.value==null){
                fetchBookList()
            }
        }
    }

    fun loadNextData(){
        if(networkResponse.value?.peekContent() != NetworkState.LOADING){
            viewModelScope.launch {
                fetchBookList(isNext = true)
            }
        }
    }

    private suspend fun fetchBookList(isNext:Boolean = false){

        if(isNext){
            requestValues = GetAudioBookListUsecase.RequestValues(pageNumber = requestValues.pageNumber.plus(1))
        }

        _networkResponse.value = Event(NetworkState.LOADING)

        useCaseHandler.execute(getAlbumListUseCase,requestValues,
            object : BaseUseCase.UseCaseCallback<GetAudioBookListUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetAudioBookListUsecase.ResponseValues) {
                    listChangedEvent.value = response.event
                    _networkResponse.value = Event(NetworkState.COMPLETED)
                    Timber.d("Data received in viewModel onSuccess")
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = Event(NetworkState.ERROR)
                    listChangedEvent.value = Event(Unit)
                    Timber.d("Data received in viewModel onError ${t.message}")
                }
            })
    }

    fun onBookItemClicked(bookId: String){
        _itemClicked.value = Event(bookId)
    }

    fun onBackArrowPressed(){
        _backArrowPressed.value = Event(true)
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}