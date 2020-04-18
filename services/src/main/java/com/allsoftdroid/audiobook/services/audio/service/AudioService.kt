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
import com.allsoftdroid.audiobook.services.audio.utils.NotificationUtils.Companion.sendNotification
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
        return audioServiceBinder
    }


    private lateinit var mediaSessionCompat : MediaSessionCompat

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

    private val mediaSessionCallback:MediaSessionCompat.Callback = object : MediaSessionCompat.Callback(){

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

        mediaSessionCompat.setCallback(mediaSessionCallback)
        val flag  = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        mediaSessionCompat.setFlags(flag)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this,MediaButtonReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(this,0,mediaButtonIntent,0)
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent)
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
                    playNextEvent()
                }
            }
        })

        disposable.add(audioServiceBinder.errorEvent.observable.subscribe {
            it.getContentIfNotHandled()?.let {errorEvent ->
                Timber.d("Received error event from AudioService binder")
                if(errorEvent){
                    Timber.d("Sending pause Event")
                    pauseEvent()
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

        initMediaSession()
        initNoisyReceiver()
    }

    private fun playNextEvent() {
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
        eventStore.publish(Event(
            Play(
                result = PlayingState(
                    playingItemIndex = audioServiceBinder.getCurrentAudioPosition(),
                    action_need = true
                )
            )
        ))
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
        applicationContext.unregisterReceiver(mNoisyReceiver)
        mediaSessionCompat.release()
        disposable.dispose()
    }
}