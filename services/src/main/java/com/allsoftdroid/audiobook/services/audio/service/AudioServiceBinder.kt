package com.allsoftdroid.audiobook.services.audio.service

import android.app.Application
import android.content.Context
import android.os.Binder
import androidx.core.net.toUri
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.audiobook.services.audio.utils.Variable
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber


class AudioServiceBinder(application: Application) : Binder(){

    private var exoPlayer: SimpleExoPlayer? = null

    private var playerPrepared: Boolean = false

    // Caller activity context, used when play local audio file.
    private var context: Context = application

    //id of playing audio book
    private lateinit var bookId:String
    private lateinit var bookName:String

    companion object {
        //song list
        private lateinit var trackList: List<AudioPlayListItem>
    }

    //current position
    private var trackPos:Int = 0

    /**
     * EXO Code start
     */

    private val playerEventListener = object : EventListener {

        override fun onPlayerError(error: ExoPlaybackException) {
            Timber.w(error, "onPlayerError type: ${error.type}")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Timber.d("onPlayerStateChanged -> playWhenReady: $playWhenReady, playbackState: $playbackState, playbackError: ${exoPlayer?.playbackError}")

            if (exoPlayer?.playbackError != null) {
                Timber.d("Error detected ")

                playerPrepared = false
                return
            }

            if (shouldPreparePlayerAgain(playWhenReady, playbackState)) {
                Timber.d("Preparing player again")
                preparePlayer()
            }

            when(playbackState){
                STATE_ENDED -> {
                    Timber.d("ENDED")
                }

                STATE_IDLE -> Timber.d("IDLE")
                STATE_BUFFERING -> Timber.d("Buffering")
                STATE_READY -> Timber.d("Ready")
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {

            exoPlayer?.let {
                Timber.d("Event ended audio about to start new")
                if(trackPos != it.currentWindowIndex){
                    nextTrackEvent.value= Event(true)
                    _trackTitle.value = trackList[it.currentWindowIndex].title?:"NA"
                    trackPos = it.currentWindowIndex
                }
            }
        }
    }

    private fun shouldPreparePlayerAgain(playWhenReady: Boolean, playbackState: Int) = playWhenReady && (playbackState == STATE_IDLE || playbackState == STATE_ENDED)

    private fun preparePlayer() {
        Timber.d("Preparing player")
        playerPrepared = true
        exoPlayer?.prepare(createMediaSource(trackList))
        exoPlayer?.seekTo(trackPos,C.TIME_UNSET)
    }

    private fun createMediaSource(playlist : List<AudioPlayListItem>): ConcatenatingMediaSource {
        Timber.d("Create media source called")
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.audio_player_service)))
        val concatenatingMediaSource = ConcatenatingMediaSource()

        Timber.d("Building media list")
        for (sample in playlist) {
            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(ArchiveUtils.getRemoteFilePath(sample.filename,bookId).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    private fun onCreate(){
        Timber.d("Created")
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()).apply {
            addListener(playerEventListener)
            Timber.d("Listener attached")
        }
        Timber.d("Player created")
    }

    fun onUnbind() {
        Timber.i("Destroyed")
        exoPlayer?.run {
            removeListener(playerEventListener)
            release()
            Timber.d("Release player")
        }

        exoPlayer = null
    }

    fun initializeAndPlay(currentPos: Int) {
        if (exoPlayer == null){
            Timber.d("Player is not yet created. starting to create")
            onCreate()
        }
        setTrackPosition(currentPos)
        Timber.d("Track pos is set.")
    }

    fun goToPreviousOrBeginning() {
        Timber.d("Previous called")
        exoPlayer?.run {
            if (hasPrevious()) {
                this.playWhenReady = true
                previous()
                if(this.currentWindowIndex>=0){
                    _trackTitle.value = trackList[this.currentWindowIndex].title?:"NA"
                }
                Timber.d("Title: ${trackTitle.value}")
            } else {
                seekToDefaultPosition()
                Timber.d("default Title: ${trackTitle.value}")
            }
        }
    }

    fun goToNext() {
        Timber.d("Next called")
        exoPlayer?.let {
            if(it.hasNext()){
                it.playWhenReady = true
                it.next()
                if(it.currentWindowIndex< trackList.size)
                {
                    _trackTitle.value = trackList[it.currentWindowIndex].title?:"NA"
                }
                Timber.d("Title: ${trackTitle.value}")
            }else{
                Timber.d("Track completed playing")
            }
        }
    }

    fun pause(){
        exoPlayer?.apply {
            this.playWhenReady = false
        }
    }

    fun resume(){
        exoPlayer?.apply {
            this.playWhenReady = true
        }
    }

    private var _trackTitle = Variable("")
    val trackTitle : Variable<String>
        get() = _trackTitle

    val nextTrackEvent = Variable(Event(false))

    fun isPlaying() = exoPlayer?.isPlaying?:false

    private fun setTrackPosition(pos:Int){
        trackPos = pos

        if (isPlaying()){
            exoPlayer?.stop()
            Timber.d("Player stopped")
        }

        exoPlayer?.let {
            it.seekTo(pos, C.TIME_UNSET)
            Timber.d("Seek is set to $pos")
            it.playWhenReady = true
            _trackTitle.value = trackList[it.currentWindowIndex].title?:"NA"
            Timber.d("Track title updated and soon will be played")
        }
    }

    fun setBookDetails(id: String, name: String){
        bookId = id
        bookName = name
        Timber.d("Book ID is $id")
    }

    fun setMultipleTracks(tracks: List<AudioPlayListItem>){
        trackList = tracks
        Timber.d("Track list set and it's size is ${tracks.size}")
    }

    fun getCurrentTrackTitle(): String = trackTitle.value

    // Return current audio play position.
    fun getCurrentAudioPosition(): Int {
        var ret = 0

        exoPlayer?.let {
            ret = it.currentWindowIndex
        }
        Timber.d("Current track pos is $ret")
        return ret
    }

    fun getBookId() = bookId
    fun getBookName() = bookName

}
