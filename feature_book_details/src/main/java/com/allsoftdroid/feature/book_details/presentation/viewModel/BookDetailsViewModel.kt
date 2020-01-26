package com.allsoftdroid.feature.book_details.presentation.viewModel

import androidx.lifecycle.*
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.store.downloader.Download
import com.allsoftdroid.common.base.store.downloader.DownloadEvent
import com.allsoftdroid.common.base.store.downloader.Downloaded
import com.allsoftdroid.common.base.store.downloader.Downloading
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.data.repository.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetDownloadUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.utils.DownloadStatusEvent
import com.allsoftdroid.feature.book_details.utils.NetworkState
import kotlinx.coroutines.*
import timber.log.Timber

class BookDetailsViewModel(
    private val sharedPref: BookDetailsSharedPreferenceRepository,
    private val stateHandle : SavedStateHandle,
    private val useCaseHandler: UseCaseHandler,
    private val getMetadataUsecase:GetMetadataUsecase,
    private val downloadUsecase: GetDownloadUsecase,
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

                    Timber.d("Current Track is $currentPlaying")

                    list[currentPlaying-1].isPlaying = false
                    list[trackNumber-1].isPlaying = true


                    _audioBookTracks.value=list.toList()

                    sharedPref.saveTrackPosition(trackNumber)
                    sharedPref.saveIsPlaying(true)
                    sharedPref.saveTrackTitle(list[trackNumber-1].title?:"N/A")
                    sharedPref.saveBookId(bookId = getMetadataUsecase.getBookIdentifier())
                    sharedPref.saveBookName(audioBookMetadata.value?.title?:"")
                    Timber.d("Track List Updated with trackNo as $trackNumber")
                }
            }
        }else{
            _audioBookTracks.value?.let {
                _audioBookTracks.value = it.toList()
            }
        }


        Timber.d("Track list updated")
        _audioBookTracks
    }

    private var job: Job? = null

    val trackFormatIndex:Int
    get() = sharedPref.trackFormatIndex()

    init {
        initialLoad()
        showPrefStat()
    }

    private fun initialLoad(){
        if(_audioBookTracks.value.isNullOrEmpty()){
            viewModelScope.launch {
                Timber.i("Starting to fetch new content from Remote repository")
                fetchMetadata()
                loadTrackWithFormat(index =
                    if(sharedPref.bookId() == getMetadataUsecase.getBookIdentifier()) sharedPref.trackFormatIndex()  else 0
                )
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

    fun downloadSelectedItemWith(trackId:String){
        viewModelScope.launch{
            withContext(Dispatchers.Main){
                audioBookTracks.value?.let { trackList ->

                    val track = trackList.find { it.trackId == trackId }

                    track?.let {
                        val album = it.trackAlbum?:getMetadataUsecase.getBookIdentifier()
                        val desc  = "Downloading: chapter ${track.trackNumber} from $album"
                        download(
                            Download(
                                bookId = getMetadataUsecase.getBookIdentifier(),
                                url = ArchiveUtils.getRemoteFilePath(filename = track.filename,identifier = getMetadataUsecase.getBookIdentifier()),
                                name = track.filename,
                                chapter = track.title?:"",
                                description = desc,
                                subPath = album,
                                chapterIndex = track.trackNumber?:0
                            )
                        )
                        Timber.d(desc)
                    }
                }
            }
        }
    }

    private suspend fun download(event:DownloadEvent){
        val requestValues = GetDownloadUsecase.RequestValues(event)

        useCaseHandler.execute(
            useCase = downloadUsecase,
            values = requestValues,
            callback = object :BaseUseCase.UseCaseCallback<GetDownloadUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetDownloadUsecase.ResponseValues) {
                    Timber.d("Download Event sent")
                }

                override suspend fun onError(t: Throwable) {
                    Timber.d("Download Event error")
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
                1 -> fetchTrackList(format = TrackFormat.FormatVBR)
                else -> fetchTrackList(format = TrackFormat.FormatBP128)
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
        Timber.d("Track number pressed for playing is :$trackNumber")
        _newTrackStateEvent.value = Event(trackNumber)
        currentPlayingTrack = trackNumber
        stateHandle.set(StateKey.CurrentPlayingTrack.key,currentPlayingTrack)
    }

    fun updateNextTrackPlaying(){
        _audioBookTracks.value?.let {trackList ->
            if(currentPlayingTrack<=trackList.size){
                var newTrack =  (currentPlayingTrack+1)%audioBookTracks.value!!.size

                if(newTrack==0) newTrack = audioBookTracks.value!!.size

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

    fun updateDownloadStatus(statusEvent:DownloadEvent) {
        _audioBookTracks.value?.let {tracks ->

            tracks[statusEvent.chapterIndex-1].downloadStatus = when(statusEvent){
                is Downloading -> {
                    Timber.d("Update list downloading tracks")
                     DownloadStatusEvent.DOWNLOADING
                }

                is Downloaded -> {
                    Timber.d("Update list for downloaded tracks")
                    DownloadStatusEvent.DOWNLOADED
                }

                else -> DownloadStatusEvent.NOTHING
            }

            _audioBookTracks.value = tracks
        }

        _newTrackStateEvent.value = Event(0)
    }

}