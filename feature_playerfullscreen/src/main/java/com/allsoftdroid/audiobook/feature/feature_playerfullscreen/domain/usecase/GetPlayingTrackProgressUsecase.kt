package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.usecase.BaseUseCase
import timber.log.Timber

class GetPlayingTrackProgressUsecase(private val audioManager: AudioManager) :
    BaseUseCase<GetPlayingTrackProgressUsecase.RequestValues, GetPlayingTrackProgressUsecase.ResponseValues>(){

    private lateinit var mainHandler: Handler

    private var _trackProgress = MutableLiveData<Int>().apply {
        this.value = 0
    }

    val trackProgress:LiveData<Int> = _trackProgress

    private var canCancelTrack:Boolean = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            val progress = audioManager.getPlayingTrackProgress()
            _trackProgress.value = progress

            Timber.d("Current Progress is $progress")

            if(progress<100){
                mainHandler.postDelayed(this, 1000)
            }
        }
    }

    public override suspend fun executeUseCase(requestValues: RequestValues?) {
        try {
            mainHandler = Handler(Looper.getMainLooper())
            start()
            useCaseCallback?.onSuccess(ResponseValues(trackProgress))
        }catch (exception:Exception){
            useCaseCallback?.onError(Error(Throwable(exception.message)))
        }
    }

    private fun stopPreviousTrackProgress(){
        if(canCancelTrack){
            cancel()
        }
    }

    fun cancel() {
        if(this::mainHandler.isInitialized){
            canCancelTrack = false
            mainHandler.removeCallbacks(updateTextTask)
        }
    }

    fun start() {
        if(this::mainHandler.isInitialized){
            stopPreviousTrackProgress()
            canCancelTrack = true
            mainHandler.post(updateTextTask)
        }
    }


    class RequestValues() : BaseUseCase.RequestValues
    class ResponseValues (val progress : LiveData<Int>) : BaseUseCase.ResponseValues
}