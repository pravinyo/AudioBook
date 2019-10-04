package com.allsoftdroid.feature.book_details.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature.book_details.data.repository.AudioBookMetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class BookDetailsViewModel(application : Application,bookId : String) : AndroidViewModel(application){
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
    private var _playItemClicked = MutableLiveData<Event<String>>()
    val playItemClicked: LiveData<Event<String>>
        get() = _playItemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    //database
    private val database = AudioBookDatabase.getDatabase(application)

    //repository reference
    private val metadataRepository = AudioBookMetadataRepositoryImpl(database.metadataDao(),bookId)

    //Book list use case
    private val getMetadataUsecase = GetMetadataUsecase(metadataRepository)

    //audio book list reference
    val audioBookMetadata: LiveData<AudioBookMetadataDomainModel>

    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            getMetadataUsecase.execute()
        }

        audioBookMetadata = getMetadataUsecase.getMetadata()
    }

    fun onPlayItemClicked(bookId: String){
        _playItemClicked.value = Event(bookId)
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