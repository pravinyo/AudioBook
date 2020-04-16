package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.usecase.BaseUseCase

class GetTrackRemainingTimeUsecase(private val audioManager: AudioManager) :
    BaseUseCase<GetTrackRemainingTimeUsecase.RequestValues, GetTrackRemainingTimeUsecase.ResponseValues>(){

    private lateinit var mainHandler: Handler

    private var _trackRemainingTime = MutableLiveData<String>().apply {
        this.value = ""
    }

    val trackRemainingTime:LiveData<String> = _trackRemainingTime

    private var canCancelTrack:Boolean = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            val seconds = audioManager.getTrackRemainingTime()

            val minutesLeft = seconds / 60
            val secondsLeft = seconds % 60

            _trackRemainingTime.value = when {
                minutesLeft>0 -> "$minutesLeft min, $secondsLeft sec remaining"
                else -> "$secondsLeft sec remaining"
            }

            mainHandler.postDelayed(this, 1000)
        }
    }

    public override suspend fun executeUseCase(requestValues: RequestValues?) {
        try {
            mainHandler = Handler(Looper.getMainLooper())
            start()
            useCaseCallback?.onSuccess(ResponseValues(trackRemainingTime))
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
        canCancelTrack = false
        mainHandler.removeCallbacks(updateTextTask)
    }

    private fun start() {
        stopPreviousTrackProgress()
        canCancelTrack = true
        mainHandler.post(updateTextTask)
    }


    class RequestValues() : BaseUseCase.RequestValues
    class ResponseValues (val progress : LiveData<String>) : BaseUseCase.ResponseValues
}