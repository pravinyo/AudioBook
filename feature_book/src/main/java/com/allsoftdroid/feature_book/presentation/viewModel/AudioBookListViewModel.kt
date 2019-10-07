package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class AudioBookListViewModel(application : Application) : AndroidViewModel(application) {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob  = SupervisorJob()

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    //track network response
    var networkResponse : LiveData<Int>? = null


    //handle item click event
    private var _itemClicked = MutableLiveData<Event<String>>()
    val itemClicked: LiveData<Event<String>>
        get() = _itemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    //database
    private val database = AudioBookDatabase.getDatabase(application)

    //repository reference
    private val bookRepository = AudioBookRepositoryImpl(database.audioBooksDao())

    //Book list use case
    private val getAlbumListUseCase = GetAudioBookListUsecase(bookRepository)

    //audio book list reference
    val audioBooks:LiveData<List<AudioBookDomainModel>>

    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            getAlbumListUseCase.execute()
        }

        networkResponse = bookRepository.response
        audioBooks = bookRepository.audioBook
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