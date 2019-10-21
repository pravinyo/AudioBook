package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class AudioBookListViewModel(
    application : Application,
    private val useCaseHandler : UseCaseHandler,
    private val getAlbumListUseCase:GetAudioBookListUsecase) : AndroidViewModel(application) {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob  = SupervisorJob()

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    //track network response
    private val _networkResponse = MutableLiveData<Int>()
    val networkResponse : LiveData<Int>
    get() = _networkResponse


    //handle item click event
    private var _itemClicked = MutableLiveData<Event<String>>()
    val itemClicked: LiveData<Event<String>>
        get() = _itemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    private val requestValues  = GetAudioBookListUsecase.RequestValues(pageNumber = 1)

    //book list state change event
    private val listChangedEvent = MutableLiveData<Event<Any>>()

    //audio book list reference
    val audioBooks:LiveData<List<AudioBookDomainModel>> = Transformations.switchMap(listChangedEvent){
        getAlbumListUseCase.getBookList()
    }

    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            fetchBookList()
        }
    }

    private suspend fun fetchBookList(){
        useCaseHandler.execute(getAlbumListUseCase,requestValues,
            object : BaseUseCase.UseCaseCallback<GetAudioBookListUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetAudioBookListUsecase.ResponseValues) {
                    listChangedEvent.value = response.event

                    Timber.d("Data received in viewModel onSuccess")
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = 0
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