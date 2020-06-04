package com.allsoftdroid.feature.book_details.presentation.viewModel

import androidx.lifecycle.*
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.WebDocument
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.FetchAdditionalBookDetailsUsecase
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.SearchBookDetailsUsecase
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.store.downloader.*
import com.allsoftdroid.common.base.store.downloader.Progress
import com.allsoftdroid.common.base.store.userAction.OpenDownloadUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.domain.usecase.*
import com.allsoftdroid.feature.book_details.utils.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

internal class BookDetailsViewModel(
    private val localFilesForBook:LocalFilesForBook,
    private val sharedPref: BookDetailsSharedPreferenceRepository,
    private val userActionEventStore: UserActionEventStore,
    private val stateHandle : SavedStateHandle,
    private val useCaseHandler: UseCaseHandler,
    private val getMetadataUsecase:GetMetadataUsecase,
    private val downloadUsecase: GetDownloadUsecase,
    private val searchBookDetailsUsecase: SearchBookDetailsUsecase,
    private val getFetchAdditionalBookDetailsUseCase: FetchAdditionalBookDetailsUsecase,
    private val listenLaterUsecase: ListenLaterUsecase,
    private val getTrackListUsecase : GetTrackListUsecase) : ViewModel(){
    private var isMultiDownloadEventSent: Boolean = false

    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob  = SupervisorJob()

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    //track network response
    private var _networkResponse = MutableLiveData<Event<NetworkState>>().also {
        it.value = Event(NetworkState.LOADING)
    }
    val networkResponse : LiveData<Event<NetworkState>>
    get() = _networkResponse


    private var currentPlayingTrack : Int = /*state.trackPlaying*/ 0
    fun getCurrentPlayingTrack() = if (currentPlayingTrack<1) 1 else currentPlayingTrack

    // when back button is pressed in the UI
    private var _backArrowPressed = MutableLiveData<Event<Boolean>>()
    val backArrowPressed: LiveData<Event<Boolean>>
        get() = _backArrowPressed

    private var _isAddedToListenLater = MutableLiveData<Event<Boolean>>()
    val isAddedToListenLater: LiveData<Event<Boolean>>
        get() = _isAddedToListenLater

    //book metadata state change event
    private val metadataStateChangeEvent = MutableLiveData<Event<Any>>()

    //audio book metadata reference
    val audioBookMetadata = Transformations.switchMap(metadataStateChangeEvent){
        getMetadataUsecase.getMetadata()
    }

    private var _additionalBookDetails = MutableLiveData<BookDetails>()
    val additionalBookDetails:LiveData<BookDetails> = _additionalBookDetails

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
    private var enhanceDetailsJob:Job?= null

    val trackFormatIndex:Int
    get() = sharedPref.trackFormatIndex()

    init {
        initialLoad()
        showPrefStat()
        isAddedToListenLater()
    }

    private fun initialLoad(){
        if(_audioBookTracks.value.isNullOrEmpty()){
            viewModelScope.launch {
                Timber.i("Starting to fetch new content from Remote repository")
                fetchMetadata()
                loadTrackWithFormat(index =
                    if(sharedPref.bookId() == getMetadataUsecase.getBookIdentifier()) sharedPref.trackFormatIndex()  else 0
                )

                audioBookMetadata.observeForever {
                    Timber.d("Metadata is available, fetching enhance details")
                    if(enhanceDetailsJob==null && it!=null){
                        enhanceDetailsJob = viewModelScope.launch {
                            fetchEnhanceDetails(title = it.title,author = it.creator)
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchEnhanceDetails(title:String,author:String) {

        Timber.d("Fetching enhanced details for title:$title and author:$author")
        val requestValues  = SearchBookDetailsUsecase.RequestValues(searchTitle =title,author = author)

        useCaseHandler.execute(
            useCase = searchBookDetailsUsecase,
            values = requestValues,
            callback = object : BaseUseCase.UseCaseCallback<SearchBookDetailsUsecase.ResponseValues> {
                override suspend fun onSuccess(response: SearchBookDetailsUsecase.ResponseValues) {
                    Timber.d("Result received for book details search : $response")

                    searchBookDetailsUsecase.getSearchBookList().observeForever {
                        Timber.d("List is => $it")
                        if(it.first().list.isNullOrEmpty() || it.first().author.isEmpty()){
                            Timber.d("It appears that book is not ready")
                            _additionalBookDetails.value = null
                        }
                    }

                    searchBookDetailsUsecase.getBooksWithRanks(title,author).observeForever {
                        if (!it.isNullOrEmpty()){
                            Timber.d("Ranks is => $it")
                            Timber.d("Selecting top item in list as best match")
                            fetchBookDetails(it.first().second)
                        }
                    }
                }

                override suspend fun onError(t: Throwable) {
                    _additionalBookDetails.value = BookDetails(chapters = emptyList())
                    Timber.d("Enhanced Error:${t.message}")
                }
            }
        )
    }

    private fun fetchBookDetails(webDocument: WebDocument) {
        Timber.d("Fetching book details for $webDocument")

        viewModelScope.launch {
            val requestValues  = FetchAdditionalBookDetailsUsecase.RequestValues(bookUrl = webDocument.url)

            getFetchAdditionalBookDetailsUseCase.getAdditionalBookDetails().observeForever {
                Timber.d("Book details fetched is : $it")
                if (it!=null){
                    it.webDocument = webDocument
                        _additionalBookDetails.value = it
                }else{
                    _additionalBookDetails.value = BookDetails(chapters = emptyList())
                }
            }

            useCaseHandler.execute(
                useCase = getFetchAdditionalBookDetailsUseCase,
                values = requestValues,
                callback = object : BaseUseCase.UseCaseCallback<FetchAdditionalBookDetailsUsecase.ResponseValues> {
                    override suspend fun onSuccess(response: FetchAdditionalBookDetailsUsecase.ResponseValues) {
                        Timber.d("Result received : ${response.details}")
                    }

                    override suspend fun onError(t: Throwable) {
                        Timber.d("Enhanced Error:${t.message}")
                    }
                }
            )
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
                    _networkResponse.value = when(t.message){
                        NetworkState.CONNECTION_ERROR.value -> Event(NetworkState.CONNECTION_ERROR)
                        else -> Event(NetworkState.SERVER_ERROR)
                    }

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
                        val id = getMetadataUsecase.getBookIdentifier()
                        downloaderAction(
                            Download(
                                bookId = id,
                                url = ArchiveUtils.getRemoteFilePath(filename = track.filename,identifier = id),
                                name = track.filename,
                                chapter = track.title?:"",
                                description = desc,
                                subPath = ArchiveUtils.getLocalSavePath(id),
                                chapterIndex = track.trackNumber?:0
                            )
                        )
                        Timber.d(desc)
                    }
                }
            }
        }
    }

    fun openDownloadsScreen(trackId:String){
        if(trackId.isNotEmpty()){
            userActionEventStore.publish(Event(OpenDownloadUI(this::class.java.simpleName)))
        }
    }

    private suspend fun downloaderAction(event:DownloadEvent){
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

                        checkLocalDownloadedFiles(it)
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

    private fun checkLocalDownloadedFiles(tracks: List<AudioBookTrackDomainModel>) {
        viewModelScope.launch {
            audioBookMetadata.value?.let { metadata ->

                val list = withContext(Dispatchers.IO){
                    localFilesForBook.getDownloadedFilesList(metadata.identifier)
                }

                list?.let {localFiles ->

                    Timber.d("Found local files: ${localFiles.size}")
                    val names = localFiles.map {
                        it.split("/").last().toLowerCase(Locale.ROOT)
                    }

                    val updatedTracks = tracks.map { track->
                        if(names.contains(track.filename.toLowerCase(Locale.ROOT))){
                            track.downloadStatus = DOWNLOADED
                        }
                        track
                    }
                    _audioBookTracks.value = updatedTracks
                }

                if(list.isNullOrEmpty()){
                    Timber.d("List is empty resetting to original value")
                    _audioBookTracks.value = tracks
                }

                _newTrackStateEvent.value = Event(true)
            }
        }
    }

    /**
     * creates a event when back arrow is pressed
     */
    fun onBackArrowPressed(){
        _backArrowPressed.value = Event(true)
    }

    fun addToListenLater(){
        var duration=""
        additionalBookDetails.value?.let {details->
            duration = details.runtime
        }
        audioBookMetadata.value?.let {metadata ->
            viewModelScope.launch {
                listenLaterUsecase.addToListenLater(
                    bookId = metadata.identifier,
                    title = metadata.title,
                    author = metadata.creator,
                    duration = if(duration.isEmpty()) metadata.runtime else duration)
                _isAddedToListenLater.value = Event(true)
            }
        }
    }

    fun removeFromListenLater(){
        viewModelScope.launch {
            listenLaterUsecase.remove(getMetadataUsecase.getBookIdentifier())
            _isAddedToListenLater.value = Event(false)
        }
    }

    private fun isAddedToListenLater(){
        viewModelScope.launch {
            val isAdded = listenLaterUsecase.isAdded(getMetadataUsecase.getBookIdentifier())
            _isAddedToListenLater.value = Event(isAdded)
        }
    }


    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        getMetadataUsecase.dispose()
    }

    fun updateDownloadStatus(statusEvent:DownloadEvent) {

        Timber.d(" download status event received")
        _audioBookTracks.value?.let {tracks ->

            Timber.d("List is non null and ready to update")

            tracks[statusEvent.chapterIndex-1].downloadStatus = when(statusEvent){
                is Downloading -> {
                    Timber.d("Event is of type Downloading:$statusEvent")
                     DOWNLOADING
                }

                is Downloaded -> {
                    Timber.d("Event is of type Downloaded:$statusEvent")
                    DOWNLOADED
                }

                is Progress -> {
                    Timber.d("Event is of type Progress:${statusEvent}")
                    PROGRESS(percent = statusEvent.percent.toFloat())
                }

                is Cancelled -> {
                    Timber.d("Event is of type Cancelled:${statusEvent}")
                    CANCELLED
                }

                else -> {
                    Timber.d("Event is of type Download:${statusEvent is Download}")
                    Timber.d("Event is of type Failed:${statusEvent is Failed}")
                    Timber.d("Event is of type DownloadNothing:${statusEvent is DownloadNothing}")
                    NOTHING
                }
            }

            Timber.i("New Track set for UI")
            _audioBookTracks.value = tracks
            if(statusEvent is Cancelled){
                _newTrackStateEvent.value = Event(true)
            }
        }
    }

    fun downloadAllChapters():Boolean {

        if (isMultiDownloadEventSent) return false

        isMultiDownloadEventSent = true

        viewModelScope.launch {
            val downloads = mutableListOf<Download>()

            audioBookMetadata.value?.let {metadata->
                audioBookTracks.value?.let {trackList->

                    trackList.map { track ->
                        downloads.add(
                            Download(
                                bookId = metadata.identifier,
                                url = ArchiveUtils.getRemoteFilePath(filename = track.filename,identifier = metadata.identifier),
                                name = track.filename,
                                chapter = track.title?:"",
                                description = "Downloading chapters for ${metadata.title}",
                                subPath = ArchiveUtils.getLocalSavePath(metadata.identifier),
                                chapterIndex = track.trackNumber?:0
                            )
                        )
                    }

                    Timber.d("Total chapters to be downloaded is ${downloads.size}")
                    downloaderAction(MultiDownload(downloads = downloads))
                }
            }
        }

        return isMultiDownloadEventSent
    }
}