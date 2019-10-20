package com.allsoftdroid.feature.book_details.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature.book_details.Utility.Utils
import com.allsoftdroid.feature.book_details.data.repository.AudioBookMetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class BookDetailsViewModel(application : Application, private val bookId : String) : AndroidViewModel(application){
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
    private var _playItemClicked = MutableLiveData<Event<Int>>()
    val playItemClicked: LiveData<Event<Int>>
        get() = _playItemClicked


    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    //audio book metadata reference
    val audioBookMetadata: LiveData<AudioBookMetadataDomainModel>

    //audio book track reference
    private var _audioBookTracks = MutableLiveData<List<AudioBookTrackDomainModel>>()
    val audioBookTracks : LiveData<List<AudioBookTrackDomainModel>>
    get() = _audioBookTracks


    //database
    private val database = AudioBookDatabase.getDatabase(application)

    //repository reference
    private val metadataRepository = AudioBookMetadataRepositoryImpl(database.metadataDao(),bookId)

    //Book metadata use case
    private val getMetadataUsecase = GetMetadataUsecase(metadataRepository)

    //Book Track list usecase
    private val getTrackListUsecase = GetTrackListUsecase(metadataRepository)


    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            getMetadataUsecase.execute()
        }

        audioBookMetadata = getMetadataUsecase.getMetadata()
        getTrackListUsecase.getTrackListData().observeForever {
            _audioBookTracks.value = it
        }
    }

    fun onPlayItemClicked(trackNumber: Int){
        //TODO: look for prints out of how to handle this situation like data is coming from course but want to
        // show change in livedata
        _audioBookTracks.value?.let {

            val list = it
            var currentPlaying = 0

            _playItemClicked.value?.let {
                currentPlaying = it.peekContent()
            }

            list[currentPlaying].isPlaying = false
            list[trackNumber-1].isPlaying = true

            _audioBookTracks.value=list.toList()
        }

        _playItemClicked.value = Event(trackNumber-1)
    }

    fun onBackArrowPressed(){
        _backArrowPressed.value = Event(true)
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * This function generated the file path on the remote server
     * @param filename unique filename on the server
     * @return complete file path on the remote server
     */
    fun getRemoteFilePath(filename: String):String{
        return Utils.getRemoteFilePath(filename,bookId)
    }
}