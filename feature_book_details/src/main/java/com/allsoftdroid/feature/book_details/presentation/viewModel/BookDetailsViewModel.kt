package com.allsoftdroid.feature.book_details.presentation.viewModel

import androidx.lifecycle.*
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.data.repository.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.presentation.NetworkState
import kotlinx.coroutines.*
import timber.log.Timber

class BookDetailsViewModel(
    private val sharedPref: BookDetailsSharedPreferenceRepository,
    private val stateHandle : SavedStateHandle,
    private val useCaseHandler: UseCaseHandler,
    private val getMetadataUsecase:GetMetadataUsecase,
    private val getTrackListUsecase : GetTrackListUsecase) : ViewModel(){
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob  = SupervisorJob()

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    //track network response
    private var _networkResponse = MutableLiveData<Event<NetworkState>>()
    val networkResponse : LiveData<Event<NetworkState>>
    get() = _networkResponse


    private var currentPlayingTrack : Int = /*state.trackPlaying*/ 0

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
                    var currentPlaying = if(currentPlayingTrack>1) currentPlayingTrack else 1
                    if(currentPlaying>list.size){
                        currentPlaying = 1
                        currentPlayingTrack = 1
                    }

                    Timber.d("Current Track is $currentPlayingTrack")

                    list[currentPlaying-1].isPlaying = false
                    list[trackNumber-1].isPlaying = true

                    _audioBookTracks.value=list.toList()

                    sharedPref.saveTrackPosition(trackNumber)
                    sharedPref.saveIsPlaying(true)
                    sharedPref.saveTrackTitle(list[trackNumber-1].title?:"N/A")
                    sharedPref.saveBookId(bookId = getMetadataUsecase.getBookIdentifier())
                    Timber.d("Track List Updated with trackNo as $trackNumber")
                }
            }
        }

        _audioBookTracks
    }

    private var job: Job? = null

    init {
        initialLoad()
        showPrefStat()
    }

    private fun initialLoad(){
        if(_audioBookTracks.value.isNullOrEmpty()){
            viewModelScope.launch {
                Timber.i("Starting to fetch new content from Remote repository")
                fetchMetadata()

                if(stateHandle.contains(StateKey.CurrentTrackFormat.key)){
                    loadTrackWithFormat(index = sharedPref.trackFormatIndex())
                }else fetchTrackList(format = TrackFormat.FormatBP64)
            }
        }
    }

    private fun showPrefStat() {
        sharedPref.trackTitle().let {
            if(it.isNotEmpty()){
                Timber.d("Track title is $it")
            }else{
                Timber.d("Track title is empty")
            }
        }

        sharedPref.trackPosition().let {
            if(it>0){
                Timber.d("Track pos is $it")
            }else{
                if(sharedPref.isPlaying()){
                    Timber.d("Track is playing and pos id  is 0")
                }else{
                    Timber.d("Track pos is 0 and not playing")
                }
            }
        }

        Timber.d("Track Book ID is ${sharedPref.bookId()}")
        Timber.d("Track format index is ${sharedPref.trackFormatIndex()}")
    }

    /**
     * Function which fetch the metadata for the book
     */
    private suspend fun fetchMetadata() {

        val requestValues  = GetMetadataUsecase.RequestValues(bookId = getMetadataUsecase.getBookIdentifier())
        _networkResponse.value = Event(NetworkState.LOADING)

        useCaseHandler.execute(
            useCase = getMetadataUsecase,
            values = requestValues,
            callback = object : BaseUseCase.UseCaseCallback<GetMetadataUsecase.ResponseValues> {
                override suspend fun onSuccess(response: GetMetadataUsecase.ResponseValues) {
                    metadataStateChangeEvent.value = response.event
                    _networkResponse.value = Event(NetworkState.COMPLETED)
                }

                override suspend fun onError(t: Throwable) {
                    _networkResponse.value = Event(NetworkState.ERROR)
                    metadataStateChangeEvent.value = Event(Unit)
                }
            }
        )
    }


    fun loadTrackWithFormat(index:Int=0){
        job?.cancel()

        job = viewModelScope.launch {
            stateHandle.set(StateKey.CurrentTrackFormat.key,index)
            when(index){
                0 -> fetchTrackList(format = TrackFormat.FormatBP64)
                1 -> fetchTrackList(format = TrackFormat.FormatBP128)
                else -> fetchTrackList(format = TrackFormat.FormatVBR)
            }
            sharedPref.saveTrackFormatIndex(index)
        }
    }

    private fun restorePreviousStateIfAny(){
        if(sharedPref.bookId() == getMetadataUsecase.getBookIdentifier()){
            Timber.d("Book id is same restoring previous state")
            currentPlayingTrack = sharedPref.trackPosition()
            _newTrackStateEvent.value = Event(sharedPref.trackPosition())
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
                        restorePreviousStateIfAny()
                    }

                    Timber.d("Track list fetch success")
                }

                override suspend fun onError(t: Throwable) {
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
        stateHandle.set(StateKey.CurrentPlayingTrack.key,currentPlayingTrack)
    }

    fun updateNextTrackPlaying(){
        _audioBookTracks.value?.let {trackList ->
            if(currentPlayingTrack<=trackList.size){
                val newTrack =  (currentPlayingTrack+1)%audioBookTracks.value!!.size
                Timber.d("New Track is $newTrack")
                onPlayItemClicked(newTrack)
            }
        }
    }

    fun updatePreviousTrackPlaying(){

        if(currentPlayingTrack>audioBookTracks.value!!.size){
            currentPlayingTrack = audioBookTracks.value!!.size
        }

        if(currentPlayingTrack>0){
            val newTrack =  if (currentPlayingTrack>1)(currentPlayingTrack-1)%audioBookTracks.value!!.size else 1
            Timber.d("Previous Track is $newTrack")
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
        getMetadataUsecase.dispose()
    }
}