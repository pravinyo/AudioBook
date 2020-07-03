package com.allsoftdroid.audiobook.services.audio.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import android.widget.Toast
import androidx.media.session.MediaButtonReceiver
import com.allsoftdroid.audiobook.services.audio.di.AudioServiceModule
import com.allsoftdroid.audiobook.services.audio.utils.NotificationUtils.Companion.sendNotification
import com.allsoftdroid.audiobook.services.audio.utils.PlayerState
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.audioPlayer.*
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
        Timber.d("Binding Audio Service")
        return audioServiceBinder
    }


    private var mediaSessionCompat : MediaSessionCompat? = null

    private val mNoisyReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                if(audioServiceBinder.isInitialized() && audioServiceBinder.isPlaying()){
                    Timber.d("Pause Media due to noisy")
                    pauseEvent()
                    Toast.makeText(context,"Pause Media due to noisy",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private var mediaSessionCallback:MediaSessionCompat.Callback? = object : MediaSessionCompat.Callback(){

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {

            mediaButtonEvent?.let { intent ->
                if(intent.action == Intent.ACTION_MEDIA_BUTTON){
                    intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.let {
                        keyEvent ->
                        when(keyEvent.action){
                            KeyEvent.ACTION_DOWN -> {
                                Timber.d("Button Media Pressed")
                                if(audioServiceBinder.isInitialized()){
                                    if(audioServiceBinder.isPlaying())
                                        pauseEvent()
                                    else
                                        playEvent()
                                }

                                Toast.makeText(applicationContext,"Button Media Pressed",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent)

        }
    }

    private fun initNoisyReceiver(){
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        applicationContext.registerReceiver(mNoisyReceiver,filter)
    }

    private fun initMediaSession(){
        val buttonReceiver = ComponentName(applicationContext,MediaButtonReceiver::class.java)
        mediaSessionCompat = MediaSessionCompat(applicationContext,"TAG",buttonReceiver,null)

        mediaSessionCompat?.let {mediaSession ->
            mediaSession.setCallback(mediaSessionCallback)
            val flag  = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            mediaSession.setFlags(flag)

            val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            mediaButtonIntent.setClass(this,MediaButtonReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(this,0,mediaButtonIntent,0)
            mediaSession.setMediaButtonReceiver(pendingIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        disposable.add(audioServiceBinder.trackTitle.observable.subscribe {
            if(it.isNotEmpty()) buildNotification(isItFirst = true)
        })

        disposable.add(audioServiceBinder.playerState.observable.subscribe { stateEvent ->
            stateEvent.getContentIfNotHandled()?.let {state->
                when(state){
                    PlayerState.SourceError,
                    PlayerState.SystemError -> {
                        notifyPlayerStateEvent(isReady = false)
                        pauseEvent()
                    }

                    PlayerState.PlayerBusy -> {
                        notifyPlayerStateEvent(isReady = false)
                        bufferingEvent()
                    }

                    PlayerState.PlayerIdle -> {
                        notifyPlayerStateEvent(isReady = false)
                    }

                    PlayerState.PlayerFinished -> {
                        pauseEvent()
                        finishEvent()
                    }

                    PlayerState.PlayerReady -> {
                        notifyPlayerStateEvent(isReady = true)
                    }

                    PlayerState.PlayingNext -> {
                        playNextEvent()
                    }
                }
            }
        })

        disposable.add(
            eventStore.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(audioServiceBinder.isInitialized()){
                        handleEvent(it)
                    }
                }
        )

        initMediaSession()
        initNoisyReceiver()
    }

    private fun bufferingEvent() {
        Timber.d("Received buffering event from AudioService binder")
        eventStore.publish(Event(Buffering))
    }

    private fun playNextEvent() {
        Timber.d("Received next event from AudioService binder")
        eventStore.publish(Event(
            Next(
                result = PlayingState(
                    playingItemIndex = audioServiceBinder.getCurrentAudioPosition() + 1,
                    action_need = false
                )
            )
        ))
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSessionCompat,intent)
        return super.onStartCommand(intent, flags, startId)
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

    private fun pauseEvent(){
        Timber.d("Sending pause Event")
        eventStore.publish(Event(
            Pause(
                result = PlayingState(
                    playingItemIndex = audioServiceBinder.getCurrentAudioPosition(),
                    action_need = true
                )
            )
        ))
    }

    private fun playEvent(){
        Timber.d("Audio Service is sending play event")
        eventStore.publish(Event(
            Play(
                result = PlayingState(
                    playingItemIndex = audioServiceBinder.getCurrentAudioPosition(),
                    action_need = true
                )
            )
        ))
    }

    private fun finishEvent(){
        Timber.d("Player has completed all the track")
        eventStore.publish(Event(Finished))
    }

    private fun notifyPlayerStateEvent(isReady:Boolean){
        Timber.d("Received player ready state event, isReady:$isReady")

        eventStore.publish(Event(
            AudioPlayerPlayingState(
                isReady = isReady
            )
        ))
    }


    private fun buildNotification( isItFirst:Boolean = false) {
        sendNotification(
            trackTitle = audioServiceBinder.getCurrentTrackTitle(),
            bookName = audioServiceBinder.getBookName(),
            applicationContext = applicationContext,
            service = this,
            isAudioPlaying = if(isItFirst) true else audioServiceBinder.isPlaying(),
            currentAudioPos = audioServiceBinder.getCurrentAudioPosition()
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        audioServiceBinder.onUnbind()
        Timber.d("Unbinding Audio Service")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.d("Destroying Audio Service 1")
        super.onDestroy()
        Timber.d("Destroying Audio Service 2")
        stopForeground(true)
        applicationContext.unregisterReceiver(mNoisyReceiver)
        mediaSessionCallback = null

        mediaSessionCompat?.let {
            it.release()
            mediaSessionCompat = null
        }

        disposable.dispose()
//        AudioServiceModule.unLoadModules()
    }
}