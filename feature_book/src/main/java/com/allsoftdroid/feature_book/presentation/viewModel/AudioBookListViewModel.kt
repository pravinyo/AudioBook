package com.allsoftdroid.feature_book.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.data.database.AudioBookDatabase
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
    private var _itemClicked = MutableLiveData<String>()
    val itemClicked: LiveData<String>
        get() = _itemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Boolean>()
    val backArrowPressed: LiveData<Boolean>
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


    fun onUserItemClicked(username: String){
        _itemClicked.value = username
    }

    fun onUserItemClickedFinished(){
        _itemClicked.value = null
    }

    fun onBackArrowPressed(){
        _backArrowPressed.value = true
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}