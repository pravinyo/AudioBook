package com.allsoftdroid.audiobook.services.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.utils.SingletonHolder
import timber.log.Timber

class AudioManager private constructor(context: Context){

    companion object : SingletonHolder<AudioManager, Context>(creator = ::AudioManager)

    var audioServiceBinder : AudioServiceBinder? = null
    private var _currentTrack = 0
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

    private val playIntent by lazy { Intent(appContext, AudioService::class.java) }

    private val audioService by lazy {
        audioServiceBinder as AudioServiceBinder
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

    fun playTrackAtPosition(trackNumber : Int?){
        if (_currentTrack != trackNumber){

            _currentTrack = trackNumber?:1
            playSelectedTrackFile(_currentTrack.minus(1))
        }
    }

    fun setPlayTrackList(playlist: List<AudioPlayListItem>,bookId: String){
        audioService.setMultipleTracks(playlist)
        audioService.setBookId(bookId)
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
        }
    }

    fun getTrackTitle() = audioService.getCurrentTrackTitle()

    fun getBookId() = audioService.getBookId()
}