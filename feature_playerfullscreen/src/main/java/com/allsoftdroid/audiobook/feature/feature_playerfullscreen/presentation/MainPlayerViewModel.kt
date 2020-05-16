package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayerControlState
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase.GetPlayingTrackProgressUsecase
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase.GetTrackRemainingTimeUsecase
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.userAction.OpenMiniPlayerUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class MainPlayerViewModel(
    private val eventStore : AudioPlayerEventStore,
    private val userActionEventStore: UserActionEventStore,
    private val useCaseHandler : UseCaseHandler,
    private val trackProgressUsecase: GetPlayingTrackProgressUsecase,
    private val remainingTimeUsecase: GetTrackRemainingTimeUsecase) : ViewModel(), KoinComponent {

    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var _playerControlState = MutableLiveData<Event<PlayerControlState>>()
    val previousControlState : LiveData<Event<PlayerControlState>> = _playerControlState

    private var _shouldItPlay:Boolean = false
    var shouldItPlay  = MutableLiveData<Boolean>()

    private var _playingTrackDetails = MutableLiveData<PlayingTrackDetails>()
    val playingTrackDetails : LiveData<PlayingTrackDetails> = _playingTrackDetails

    private var currentPlayingIndex = 0

    val trackProgress:LiveData<Int>
    get() = trackProgressUsecase.trackProgress

    val trackRemainingTime:LiveData<String>
    get() = remainingTimeUsecase.trackRemainingTime

    fun playPrevious(){
        _playerControlState.value = Event(PlayerControlState(playPrevious = true))

        Timber.d("Sending new Previous event")
        eventStore.publish(
            Event(
                Previous(
                    PlayingState(
                        playingItemIndex = currentPlayingIndex - 1,
                        action_need = true
                    )
                )
            )
        )
    }

    fun goBackward(){

    }

    fun goForward(){

    }

    fun playNext(){
        _playerControlState.value = Event(PlayerControlState(playNext = true))

        Timber.d("Sending new next event")
        eventStore.publish(
            Event(
                Next(
                    PlayingState(
                        playingItemIndex = currentPlayingIndex + 1,
                        action_need = true
                    )
                )
            )
        )
    }

    fun playPause(){
        setShouldPlay(!_shouldItPlay)
        _playerControlState.value = Event(PlayerControlState(shouldItPlay = _shouldItPlay))
        shouldPlayEvent()
    }

    fun setBookDetails(bookId:String, bookName:String, trackName:String, currentPlayingTrack: Int, totalChapter:Int,isPlaying:Boolean){

        _playingTrackDetails.value = PlayingTrackDetails(
            bookIdentifier = bookId,
            bookTitle = bookName,
            trackName = trackName,
            chapterIndex = currentPlayingTrack,
            totalChapter = totalChapter,
            isPlaying = isPlaying
        )

        currentPlayingIndex = currentPlayingTrack

        viewModelScope.launch {
            initTrackProgress()
            initTrackRemainingTimeStatus()
        }
    }

    fun updateTrackDetails(chapterIndex:Int,chapterTitle:String){
        _playingTrackDetails.value?.let {
            setBookDetails(it.bookIdentifier,it.bookTitle,chapterTitle,chapterIndex,it.totalChapter,it.isPlaying)
        }
    }

    private fun setShouldPlay(play:Boolean){
        _shouldItPlay = play
        shouldItPlay.value = _shouldItPlay
    }

    private fun shouldPlayEvent(){
        Timber.d("should play event is $_shouldItPlay")

        if(_shouldItPlay){
            Timber.d("Sending new play event")
            eventStore.publish(
                Event(
                    Play(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                )
            )
        }else{
            Timber.d("Sending new pause event")
            eventStore.publish(
                Event(
                    Pause(
                        PlayingState(
                            playingItemIndex = currentPlayingIndex,
                            action_need = true
                        )
                    )
                )
            )
        }
    }

    fun showMiniPlayer(){
        userActionEventStore.publish(Event(OpenMiniPlayerUI(this::class.java.simpleName)))
    }

    private suspend fun initTrackProgress() {
        useCaseHandler.execute(trackProgressUsecase,GetPlayingTrackProgressUsecase.RequestValues(),
            object : BaseUseCase.UseCaseCallback<GetPlayingTrackProgressUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetPlayingTrackProgressUsecase.ResponseValues) {
                    Timber.d("Track Progress initialization completed")
                }

                override suspend fun onError(t: Throwable) {
                    Timber.d("Track Progress init error: ${t.message}")
                }
            })
    }

    private suspend fun initTrackRemainingTimeStatus() {
        useCaseHandler.execute(remainingTimeUsecase,GetTrackRemainingTimeUsecase.RequestValues(),
            object : BaseUseCase.UseCaseCallback<GetTrackRemainingTimeUsecase.ResponseValues>{
                override suspend fun onSuccess(response: GetTrackRemainingTimeUsecase.ResponseValues) {
                    Timber.d("Track remaining time initialization completed")
                }

                override suspend fun onError(t: Throwable) {
                    Timber.d("Track remaining time init error: ${t.message}")
                }
            })
    }

    fun resumeProgressTracking(){
        trackProgressUsecase.start()
        remainingTimeUsecase.start()
    }

    fun stopProgressTracking(){
        trackProgressUsecase.cancel()
        remainingTimeUsecase.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        viewModelJob.cancel()
    }
}