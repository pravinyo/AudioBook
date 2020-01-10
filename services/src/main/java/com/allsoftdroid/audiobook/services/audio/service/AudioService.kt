package com.allsoftdroid.audiobook.services.audio.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.allsoftdroid.audiobook.services.audio.utils.NotificationUtils.Companion.sendNotification
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import timber.log.Timber


class AudioService : Service(),KoinComponent{

    companion object CONSTANT{
        const val ACTION_PREVIOUS = 0
        const val PREVIOUS="previous"

        const val ACTION_PLAY_PAUSE=1
        const val PLAY="play"
        const val PAUSE="pause"

        const val ACTION_NEXT = 2
        const val NEXT="next"
    }

    private val audioServiceBinder:AudioServiceBinder by inject()

    private val eventStore : AudioPlayerEventStore by inject()

    private var disposable:CompositeDisposable = CompositeDisposable()

    override fun onBind(p0: Intent?): IBinder? {
        return audioServiceBinder
    }

    override fun onCreate() {
        super.onCreate()

        disposable.add(audioServiceBinder.trackTitle.observable.subscribe {
            if(it.isNotEmpty()) buildNotification(isItFirst = true)
        })

        disposable.add(audioServiceBinder.nextTrackEvent.observable.subscribe {
            it.getContentIfNotHandled()?.let {nextEvent ->
                Timber.d("Received next event from AudioService binder")
                if(nextEvent){
                    Timber.d("Sending next Event")
                    eventStore.publish(Event(Next( result = PlayingState(
                        playingItemIndex = audioServiceBinder.getCurrentAudioPosition()+1,
                        action_need = false
                    ))))
                }
            }
        })

        disposable.add(audioServiceBinder.errorEvent.observable.subscribe {
            it.getContentIfNotHandled()?.let {errorEvent ->
                Timber.d("Received error event from AudioService binder")
                if(errorEvent){
                    Timber.d("Sending pause Event")
                    eventStore.publish(Event(Pause( result = PlayingState(
                        playingItemIndex = audioServiceBinder.getCurrentAudioPosition(),
                        action_need = true
                    ))))
                }
            }
        })

        disposable.add(
            eventStore.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    handleEvent(it)
                }
        )
    }

    private fun handleEvent(event: Event<AudioPlayerEvent>?) {
        event?.let {
            if(it.hasBeenHandled){
                when(it.peekContent()){
                    is Play -> {
                        buildNotification()
                    }

                    is Pause -> {
                        buildNotification()
                    }
                }
            }
        }
    }


    private fun buildNotification( isItFirst:Boolean = false) {
        sendNotification(
            trackTitle = audioServiceBinder.getCurrentTrackTitle(),
            bookId = audioServiceBinder.getBookId(),
            bookName = audioServiceBinder.getBookName(),
            applicationContext = applicationContext,
            service = this,
            isAudioPlaying = if(isItFirst) true else audioServiceBinder.isPlaying(),
            currentAudioPos = audioServiceBinder.getCurrentAudioPosition()
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        audioServiceBinder.onUnbind()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        disposable.dispose()
    }
}