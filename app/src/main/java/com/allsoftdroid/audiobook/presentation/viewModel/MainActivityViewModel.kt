package com.allsoftdroid.audiobook.presentation.viewModel

import android.app.Application
import androidx.lifecycle.*
import com.allsoftdroid.audiobook.domain.model.LastPlayedTrack
import com.allsoftdroid.audiobook.domain.usecase.GetLastPlayedUsecase
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivityViewModel(application : Application,
                            private val stateHandle : SavedStateHandle,
                            private val handler: UseCaseHandler,
                            private val lastPlayedUsecase: GetLastPlayedUsecase,
                            private val sharedPref: BookDetailsSharedPreferenceRepository,
                            private val eventStore : AudioPlayerEventStore,
                            private val audioManager : AudioManager) : AndroidViewModel(application){

    companion object{
        private const val MINI_PLAYER_KEY = "MiniPlayerKey"
    }

    private val _showMiniPlayer = MutableLiveData<Event<Boolean>>()
    val showPlayer :LiveData<Boolean> = stateHandle.getLiveData(MINI_PLAYER_KEY)

    private var disposable : Disposable

    private var _playerEvent  = MutableLiveData<Event<AudioPlayerEvent>>()
    val playerEvent : LiveData<Event<AudioPlayerEvent>> = _playerEvent

    private var _lastPlayed = MutableLiveData<Event<LastPlayedTrack>>()
    val lastPlayed :LiveData<Event<LastPlayedTrack>> = _lastPlayed

    private lateinit var _playingTrackDetails:PlayingTrackDetails

    fun getPlayingTrack():PlayingTrackDetails? = _playingTrackDetails

    fun playerStatus( showPlayer : Boolean){
        _showMiniPlayer.value = Event(showPlayer)
        stateHandle.set(MINI_PLAYER_KEY,showPlayer)
    }

    init {

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _playerEvent.value = it
            }

        checkLastPlayed()
    }

    fun playSelectedTrack(event: PlaySelectedTrack){
        audioManager.setPlayTrackList(event.trackList,event.bookId,event.bookName)
        audioManager.playTrackAtPosition(trackNumber = event.position,bookId = event.bookId)

        eventStore.publish(Event(
            TrackDetails(
                trackTitle = audioManager.getTrackTitle(),
                bookId = audioManager.getBookId(),
                position = event.position
            )
        ))

        _playingTrackDetails = PlayingTrackDetails(
            bookIdentifier = audioManager.getBookId(),
            bookTitle = event.bookName,
            trackName = audioManager.getTrackTitle(),
            chapterIndex = event.position,
            totalChapter = event.trackList.size,
            isPlaying = true
        )

        Timber.d("Play selected track event:track item position:${event.position} and bookid:${event.bookId} and name:${event.bookName}")
    }

    fun nextTrack(event: Next){
        val state = event.result as PlayingState

        if(state.action_need) audioManager.playNext()

        eventStore.publish(Event(
            TrackDetails(
                trackTitle = audioManager.getTrackTitle(),
                bookId = audioManager.getBookId(),
                position = audioManager.currentPlayingIndex() + 1
            )
        ))

        _playingTrackDetails.chapterIndex = audioManager.currentPlayingIndex() + 1
        _playingTrackDetails.isPlaying = true

        Timber.d("Next event occurred")
    }

    fun previousTrack(){

        audioManager.playPrevious()

        eventStore.publish(Event(
            TrackDetails(
                trackTitle = audioManager.getTrackTitle(),
                bookId = audioManager.getBookId(),
                position = audioManager.currentPlayingIndex() + 1
            )
        ))
        _playingTrackDetails.chapterIndex = audioManager.currentPlayingIndex() + 1
        _playingTrackDetails.isPlaying = true

        Timber.d("Previous event occur")
    }

    fun pauseTrack(){
        audioManager.pauseTrack()
        _playingTrackDetails.isPlaying = false
        Timber.d("pause event")
    }

    fun rewindTrack(){
        audioManager.rewindPlayer(30*1000)
        _playingTrackDetails.isPlaying = true
        Timber.d("rewind event")
    }

    fun forwardTrack(){
        audioManager.forwardPlayer(30*1000)
        _playingTrackDetails.isPlaying = true
        Timber.d("forward event")
    }

    fun resumeOrPlayTrack(){
        if(this::_playingTrackDetails.isInitialized){
            audioManager.resumeTrack()
            _playingTrackDetails.isPlaying = true
            Timber.d("Play/Resume track event")
        }
    }

    fun playIfAnyTrack(){

        if(audioManager.isServiceReady()){
            eventStore.publish(Event(
                Play(PlayingState(
                    playingItemIndex = audioManager.currentPlayingIndex(),
                    action_need = true
                ))
            ))

            Timber.d("Play if any pending tracks event")
        }
    }

    fun bindAudioService(){
        audioManager.bindAudioService()
    }

    fun unBoundAudioService(){
        audioManager.unBoundAudioService()
    }


    fun clearSharedPref(){
        sharedPref.clear()
        Timber.d("Cleared Shared Pref")
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    private fun checkLastPlayed(){
        val request = GetLastPlayedUsecase.RequestValues(sharedPref = sharedPref)

        viewModelScope.launch {
            handler.execute(lastPlayedUsecase,request,object : BaseUseCase.UseCaseCallback<GetLastPlayedUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetLastPlayedUsecase.ResponseValues) {
                    response.lastPlayedTrack?.let {
                        _lastPlayed.value = Event(it)
                    }
                }

                override suspend fun onError(t: Throwable) {

                }
            })
        }
    }
}