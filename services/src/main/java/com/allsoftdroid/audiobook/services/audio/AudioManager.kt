package com.allsoftdroid.audiobook.services.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.allsoftdroid.audiobook.services.audio.di.AudioServiceModule
import com.allsoftdroid.audiobook.services.audio.service.AudioServiceBinder
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.utils.SingletonHolder
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class AudioManager private constructor(context: Context):KoinComponent{

    companion object : SingletonHolder<AudioManager, Context>(creator = ::AudioManager)

    var audioServiceBinder : AudioServiceBinder? = null
    private var _currentTrack = 0
    private var mBookId:String =""
    private val appContext : Context = context

    // This service connection object is the bridge between activity and background service.
    private val serviceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                // Cast and assign background service's onBind method returned iBander object.
                val service = iBinder as AudioServiceBinder
                audioServiceBinder = service
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                audioServiceBinder = null
            }
        }
    }

    // Bind background service with caller . Then this caller can use
    // background service's AudioServiceBinder instance to invoke related methods.

    private val playIntent:Intent by inject()

    private val audioService by lazy {
        audioServiceBinder as AudioServiceBinder
    }


    init {
        AudioServiceModule.injectFeature()
    }


    fun bindAudioService() {
        if (audioServiceBinder == null) {
            // Below code will invoke serviceConnection's onServiceConnected method.
            appContext.bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            appContext.startService(playIntent)
        }
    }

    // Unbound background audio service with caller activity.
    fun unBoundAudioService() {
        if (audioServiceBinder != null) {
            appContext.unbindService(serviceConnection)
            appContext.stopService(playIntent)
        }
    }

    fun playTrackAtPosition(trackNumber : Int?,bookId: String){
        if (_currentTrack != trackNumber || mBookId != bookId){
            mBookId = bookId
            _currentTrack = trackNumber?:1
            Timber.d("Manager: Play track at position :$trackNumber")
            playSelectedTrackFile(_currentTrack.minus(1))
        }else{
            Timber.d("Manager: Ignored Play track at position :$trackNumber")
        }
    }

    fun setPlayTrackList(playlist: List<AudioPlayListItem>,bookId: String,bookName:String){
        audioService.setMultipleTracks(playlist)
        audioService.setBookDetails(bookId,bookName)
    }

    private fun playSelectedTrackFile(currentPos:Int) {
        audioService.initializeAndPlay(currentPos)
    }

    fun playNext(){
        Timber.d("Playing next Audio File")
        audioService.goToNext()
    }

    fun playPrevious(){
        audioService.goToPreviousOrBeginning()
    }

    fun pauseTrack(){
        if (audioService.isPlaying()){
            audioService.pause()
            Timber.d("Pause pressed")
        }
    }

    fun resumeTrack(){
        if (!audioService.isPlaying()){
            audioService.resume()
            Timber.d("Resume pressed")
        }else{
            Timber.d("Resume pressed but track is already playing")
        }
    }

    fun isPlaying() = audioService.isPlaying()

    fun currentPlayingIndex() = audioService.getCurrentAudioPosition()

    fun getTrackTitle() = audioService.getCurrentTrackTitle()

    fun getBookId() = audioService.getBookId()

    /**
     * Return the time elapsed playing the current track
     */
    fun getPlayingTrackProgress():Float{
        return 0f
    }
}