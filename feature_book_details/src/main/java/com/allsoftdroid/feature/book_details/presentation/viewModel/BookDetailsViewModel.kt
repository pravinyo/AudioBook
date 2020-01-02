package com.allsoftdroid.feature.book_details.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.data.repository.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import kotlinx.coroutines.*
import timber.log.Timber

class BookDetailsViewModel(
    application : Application,
    private val useCaseHandler: UseCaseHandler,
    private val getMetadataUsecase:GetMetadataUsecase,
    private val getTrackListUsecase : GetTrackListUsecase) : AndroidViewModel(application){
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob  = SupervisorJob()

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    //track network response
    private var _networkResponse = MutableLiveData<Int>()
    val networkResponse : LiveData<Int>
    get() = _networkResponse


    private var currentPlayingTrack : Int = /*state.trackPlaying*/ 0
    //handle item click event
    private var _playItemClicked = MutableLiveData<Event<Int>>()

    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed


    //book metadata state change event
    private val metadataStateChangeEvent = MutableLiveData<Event<Any>>()

    //audio book metadata reference
    val audioBookMetadata = Transformations.switchMap(metadataStateChangeEvent){
        getMetadataUsecase.getMetadata()
    }

    //track state event
    private val _newTrackStateEvent = MutableLiveData<Event<Any>>() //holds track number clicked by user

    //audio book track reference
    private var _audioBookTracks = MutableLiveData<List<AudioBookTrackDomainModel>>()

    //get updated track list on track state change
    val audioBookTracks : LiveData<List<AudioBookTrackDomainModel>> =
        Transformations.switchMap(_newTrackStateEvent){trackNumberEvent ->

        val trackNumber = trackNumberEvent.getContentIfNotHandled()?:trackNumberEvent.peekContent()

        if (trackNumber is Int && trackNumber>0){

            _audioBookTracks.value?.let {

                val list = it
                if(list.size>=trackNumber){
                    var currentPlaying = 0

                    _playItemClicked.value?.let {event ->
                        currentPlaying = event.peekContent()
                    }

                    list[currentPlaying].isPlaying = false
                    list[trackNumber-1].isPlaying = true

                    _audioBookTracks.value=list.toList()

                    _playItemClicked.value = Event(trackNumber-1)
                    Timber.d("Track List Updated with trackNo as $trackNumber")
                }
            }
        }

        _audioBookTracks
    }

    private var job: Job? = null

    init {
        viewModelScope.launch {
            Timber.i("Starting to fetch new content from Remote repository")
            fetchMetadata()
            fetchTrackList(format = TrackFormat.FormatBP64)
        }
    }

    /**
     * Function which fetch the metadata for the book
     */
    private suspend fun fetchMetadata() {

        val requestValues  = GetMetadataUsecase.RequestValues(bookId = getMetadataUsecase.getBookIdentifier())

        useCaseHandler.execute(
            useCase = getMetadataUsecase,
            values = requestValues,
            callback = object : BaseUseCase.UseCaseCallback<GetMetadataUsecase.ResponseValues> {
                override suspend fun onSuccess(response: GetMetadataUsecase.ResponseValues) {
                    metadataStateChangeEvent.value = response.event
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = 0
                    metadataStateChangeEvent.value = Event(Unit)
                }
            }
        )
    }


    fun loadTrackWithFormat(index:Int){
        job?.cancel()

        job = viewModelScope.launch {
            when(index){
                0 -> fetchTrackList(format = TrackFormat.FormatBP64)
                1 -> fetchTrackList(format = TrackFormat.FormatBP128)
                else -> fetchTrackList(format = TrackFormat.FormatVBR)
            }
        }
    }
    /**
     * Function which fetch track list for the book
     */
    private suspend fun fetchTrackList(format: TrackFormat){
        val requestValues  = GetTrackListUsecase.RequestValues(trackFormat = format)

        useCaseHandler.execute(
            useCase = getTrackListUsecase,
            values = requestValues,
            callback = object : BaseUseCase.UseCaseCallback<GetTrackListUsecase.ResponseValues> {
                override suspend fun onSuccess(response: GetTrackListUsecase.ResponseValues) {

                    getTrackListUsecase.getTrackListData().observeForever {
                        _audioBookTracks.value = it
                        _newTrackStateEvent.value = response.event
                    }

                    Timber.d("Track list fetch success")
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = 0
                    _newTrackStateEvent.value = Event(Unit)
                }
            }
        )
    }

    /**
     * Creates a event when play item is clicked from the track list
     */
    fun onPlayItemClicked(trackNumber: Int){
        _newTrackStateEvent.value = Event(trackNumber)
        currentPlayingTrack = trackNumber
    }

    fun updateNextTrackPlaying(){
        _audioBookTracks.value?.let {trackList ->
            if(currentPlayingTrack<trackList.size){
                val newTrack =  (currentPlayingTrack+1)%audioBookTracks.value!!.size
                onPlayItemClicked(newTrack)
            }
        }
    }

    fun updatePreviousTrackPlaying(){

        if(currentPlayingTrack>0){
            val newTrack =  if (currentPlayingTrack>1)(currentPlayingTrack-1)%audioBookTracks.value!!.size else 1
            onPlayItemClicked(newTrack)
        }
    }

    /**
     * creates a event when back arrow is pressed
     */
    fun onBackArrowPressed(){
        _backArrowPressed.value = Event(true)
    }


    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}