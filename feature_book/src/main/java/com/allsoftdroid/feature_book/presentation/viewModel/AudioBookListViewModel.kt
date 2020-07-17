package com.allsoftdroid.feature_book.presentation.viewModel

import androidx.lifecycle.*
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature_book.di.FeatureBookModule.SUPER_VISOR_JOB
import com.allsoftdroid.feature_book.di.FeatureBookModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.domain.usecase.GetSearchBookUsecase
import com.allsoftdroid.feature_book.utils.NetworkState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class AudioBookListViewModel(
    private val useCaseHandler : UseCaseHandler,
    private val getAlbumListUseCase:GetAudioBookListUsecase,
    private val getSearchBookUsecase: GetSearchBookUsecase) : ViewModel(),KoinComponent {
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


    //close or search btn
    private var _searchOrClose = MutableLiveData<Boolean>().apply {
        value = false
    }
    val searchOrClose : LiveData<Boolean> = _searchOrClose

    //handle item click event
    private var _itemClicked = MutableLiveData<Event<String>>()
    val itemClicked: LiveData<Event<String>>
        get() = _itemClicked

    private var bookListRequestValues  = GetAudioBookListUsecase.RequestValues(pageNumber = 1)
    private var searchBookRequestValues = GetSearchBookUsecase.RequestValues(query = "",pageNumber = 0)

    //book list state change event
    private val listChangedEvent = MutableLiveData<Event<Any>>()

    //audio book list reference
    val audioBooks:LiveData<List<AudioBookDomainModel>> = Transformations.switchMap(listChangedEvent){
        getAlbumListUseCase.getBookList().asLiveData(viewModelScope.coroutineContext)
    }

    val searchBooks = MutableLiveData<List<AudioBookDomainModel>>()

    private var _displaySearchView= MutableLiveData<Boolean>()
    val displaySearch:LiveData<Boolean>
    get() = _displaySearchView

    private var _isSearching:Boolean = false
    val isSearching
    get() = _isSearching

    private var mSearchQuery:String = ""
    private var searchJob:Job? = null
    private var refreshJob:Job? = null

    private var _searchError = MutableLiveData<Boolean>()
    val searchError : LiveData<Boolean> = _searchError

    init {
        if(audioBooks.value==null) loadRecentBookList()
        _displaySearchView.value = false
        _searchError.value = false
    }

    fun loadRecentBookList(){
        if(networkResponse.value?.peekContent() != NetworkState.LOADING){
            refreshJob = viewModelScope.launch {
                Timber.i("Starting to fetch new content from Remote repository")
                if(audioBooks.value==null || _isSearching){
                    _isSearching = false
                    fetchBookList()
                }
            }
        }
    }

    fun refresh(){
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            fetchBookList()
        }
    }

    fun loadNextData(){
        if(networkResponse.value?.peekContent() != NetworkState.LOADING){
            viewModelScope.launch {
                if(isSearching) searchBook(mSearchQuery,isNext = true)
                else fetchBookList(isNext = true)
            }
        }
    }

    private suspend fun fetchBookList(isNext:Boolean = false){

        if(isNext){
            bookListRequestValues = GetAudioBookListUsecase.RequestValues(pageNumber = bookListRequestValues.pageNumber.plus(1))
        }

        _networkResponse.value = Event(NetworkState.LOADING)

        useCaseHandler.execute(getAlbumListUseCase,bookListRequestValues,
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

    @ExperimentalCoroutinesApi
    fun search(query:String, isNext: Boolean= false){
        _isSearching = true
        _searchError.value = false

        if(networkResponse.value?.peekContent() != NetworkState.LOADING){
            cancelSearchRequest()
        }

        searchJob = viewModelScope.launch {
            searchBook(searchQuery = query,isNext = isNext)
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun searchBook(searchQuery:String, isNext: Boolean){
        searchBookRequestValues = if(isNext){
            GetSearchBookUsecase.RequestValues(
                query = searchQuery,
                pageNumber = searchBookRequestValues.pageNumber.plus(1))
        }else{
            mSearchQuery = searchQuery
            searchBooks.value = emptyList()

            GetSearchBookUsecase.RequestValues(
                query = searchQuery,
                pageNumber = 1)
        }

        _networkResponse.value = Event(NetworkState.LOADING)

        useCaseHandler.execute(getSearchBookUsecase,searchBookRequestValues,
            object : BaseUseCase.UseCaseCallback<GetSearchBookUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetSearchBookUsecase.ResponseValues) {
                    listChangedEvent.value = response.event
                    _networkResponse.value = Event(NetworkState.COMPLETED)
                    Timber.d("Data received in viewModel onSuccess")

                    getSearchBookUsecase.getSearchResults().distinctUntilChanged().collect { list ->
                        when {
                            list.isNullOrEmpty() -> {
                                _searchError.value = true
                            }
                            searchBooks.value.isNullOrEmpty() -> {
                                searchBooks.value = list
                            }
                            else -> {
                                searchBooks.value?.let {prevList ->
                                    getSearchBookUsecase.getSearchResults().collect {response ->
                                        val temp = prevList.toMutableList()
                                        temp.addAll(response.asIterable())
                                        searchBooks.value = temp
                                    }
                                }
                            }
                        }
                    }
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = Event(NetworkState.ERROR)
                    listChangedEvent.value = Event(Unit)
                    Timber.d("Data received in viewModel onError ${t.message}")
                }
            })
    }

    fun cancelSearchRequest(){
        searchJob?.cancel()
    }

    fun onBookItemClicked(bookId: String){
        _itemClicked.value = Event(bookId)
    }

    fun onSearchItemPressed(){
        _displaySearchView.value = true
    }

    fun onSearchFinished(){
        _displaySearchView.value = false
        _searchError.value = false
    }

    fun setSearchOrClose(isSearchBtn:Boolean){
        if (_searchOrClose.value != isSearchBtn){
            _searchOrClose.value = isSearchBtn
        }
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        getAlbumListUseCase.cancelRequestInFlight()
        getSearchBookUsecase.cancelRequestInFlight()
    }
}