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


    private val playIntent:Intent by inject()

    private val audioService by lazy {
        audioServiceBinder as AudioServiceBinder
    }


    /**
     * On Instance creation, inject all the required dependencies
     */
    init {
        AudioServiceModule.injectFeature()
    }


    /**
     * This function will bind service and  start the service in foreground
     * It will start service if  @[audioServiceBinder] is null.
     */
    fun bindAudioService() {
        if (audioServiceBinder == null) {
            // Below code will invoke serviceConnection's onServiceConnected method.
            appContext.bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            appContext.startService(playIntent)
        }
    }

    /**
     * This function will unbind service and  stop the service from running
     * It will stop service if  @[audioServiceBinder] is available and not null
     */
    fun unBoundAudioService() {
        if (audioServiceBinder != null) {
            appContext.unbindService(serviceConnection)
            appContext.stopService(playIntent)
        }
    }

    /**
     * This  function checks whether request for new @[trackNumber] is same and from same @[bookId] or not.
     * It will trigger playing of provided @[AudioPlayListItem] only, If @[trackNumber] provided is different from @[_currentTrack]
     * or @[bookId] provided is different from @[mBookId]
     *
     * @param trackNumber
     * Integer value for @[AudioPlayListItem] number(index start from 1) from playlist
     * @param bookId
     * String value whcih holds Unique book identifier
     */
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

    /**
     * This function set new playlist to be played and book details like name and identifier for later use
     * @param playlist
     * List of @[AudioPlayListItem] to be played
     * @param bookId
     * Unique identifier of the book
     * @param bookName
     * Name of the Book
     */
    fun setPlayTrackList(playlist: List<AudioPlayListItem>,bookId: String,bookName:String){
        audioService.setMultipleTracks(playlist)
        audioService.setBookDetails(bookId,bookName)
    }

    /**
     * It is a private method which reinitialize the player and plays the @[AudioPlayListItem] from the defined @[AudioPlayListItem] location in the playlist
     * @param currentPos
     * It is the position of the @[AudioPlayListItem] to be played from the playlist
     */
    private fun playSelectedTrackFile(currentPos:Int) {
        audioService.initializeAndPlay(currentPos)
    }

    /**
     * This function calls next on the playlist items to play next @[AudioPlayListItem]
     */
    fun playNext(){
        Timber.d("Playing next Audio File")
        audioService.goToNext()
    }

    /**
     * This function calls previous or beginning(if it is first @[AudioPlayListItem] in the playlist) of the @[AudioPlayListItem]
     */
    fun playPrevious(){
        audioService.goToPreviousOrBeginning()
    }

    /**
     * This function pause the currently playing @[AudioPlayListItem]:
     * It first checks whether any @[AudioPlayListItem] is playing or not.
     * If playing then calls @[pauseTrack] method on running foreground service
     * else it will ignore the request with log details
     */
    fun pauseTrack(){
        if (audioService.isPlaying()){
            audioService.pause()
            Timber.d("Pause pressed")
        }
    }

    /**
     * This function resumes the currently playing @[AudioPlayListItem]:
     * It first checks whether any @[AudioPlayListItem] is playing or not.
     * If not playing then calls @[resumeTrack] method on running foreground service
     * else it will ignore the request with log details
     */
    fun resumeTrack(){
        if (!audioService.isPlaying()){
            audioService.resume()
            Timber.d("Resume pressed")
        }else{
            Timber.d("Resume pressed but track is already playing")
        }
    }

    /**
     * Returns the player status whether it is playing any @[AudioPlayListItem] or not
     */
    fun isPlaying() = audioService.isPlaying()

    /**
     * Returns the @[AudioPlayListItem] index(index start from 0) from the @[AudioPlayListItem] playlist
     */
    fun currentPlayingIndex() = audioService.getCurrentAudioPosition()

    /**
     * Returns the @[AudioPlayListItem] title of the currently playing track
     */
    fun getTrackTitle() = audioService.getCurrentTrackTitle()

    /**
     * Returns the book identifier of the currently playing @[AudioPlayListItem]
     */
    fun getBookId() = audioService.getBookId()

    /**
     * Return the time elapsed playing the current @[AudioPlayListItem]
     */
    fun getPlayingTrackProgress():Float{
        return 0f
    }
}