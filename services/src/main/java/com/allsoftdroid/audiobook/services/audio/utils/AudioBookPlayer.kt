package com.allsoftdroid.audiobook.services.audio.utils

import android.app.Application
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import timber.log.Timber

class AudioBookPlayer(private val context:Application,
                      private val prepareMediaHandler: PrepareMediaHandler) {

    //current position
    private var trackPos:Int = 0

    private var _trackTitle = Variable("")
    val trackTitle : Variable<String>
        get() = _trackTitle

    private lateinit var trackList: List<AudioPlayListItem>
    private lateinit var bookId:String

    private var exoPlayer: SimpleExoPlayer? = null

    private val playerEventListener = object : Player.EventListener {

        override fun onPlayerError(error: ExoPlaybackException) {
            Timber.w(error, "onPlayerError type: ${error.type}")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Timber.d("onPlayerStateChanged -> playWhenReady: $playWhenReady, playbackState: $playbackState, playbackError: ${exoPlayer?.playbackError}")

            if (exoPlayer?.playbackError != null) {
                Timber.d("Error detected ")

                exoPlayer?.playbackError?.let {
                    when(it.type){
                        ExoPlaybackException.TYPE_SOURCE->{
                            Timber.d("Source Error detected :${it.message}")
                        }
                        ExoPlaybackException.TYPE_REMOTE->{
                            Timber.d("Remote Error detected :${it.message}")
                        }
                        ExoPlaybackException.TYPE_OUT_OF_MEMORY->{
                            Timber.d("Memory Error detected :${it.message}")
                        }
                        ExoPlaybackException.TYPE_RENDERER->{
                            Timber.d("Renderer Error detected :${it.message}")
                        }
                        ExoPlaybackException.TYPE_UNEXPECTED->{
                            Timber.d("Unexpected Error detected :${it.message}")
                        }
                    }
                }

                errorEvent.value = Event(true)
                Timber.d("Is playing :${exoPlayer?.isPlaying}")
                isPlayerReadyEvent.value = Event(false)
                Timber.d("Player is not ready to play")
                return
            }

            if (shouldPreparePlayerAgain(playWhenReady, playbackState)) {
                if (playbackState != Player.STATE_ENDED){
                    Timber.d("Preparing player again")

                    if(bookId.isNotEmpty() && trackList.isNotEmpty()){
                        preparePlayer(bookId,trackList)
                    }
                }
            }

            when(playbackState){
                Player.STATE_ENDED -> {
                    Timber.d("ENDED")
                    errorEvent.value = Event(true)
                }

                Player.STATE_IDLE -> {
                    Timber.d("IDLE")
                    isPlayerReadyEvent.value = Event(false)
                    Timber.d("Player is not ready to play")
                }
                Player.STATE_BUFFERING -> {
                    Timber.d("Buffering")
                    isPlayerReadyEvent.value = Event(false)
                    Timber.d("Player is still bufferring")
                }
                Player.STATE_READY -> {
                    Timber.d("Ready")
                    isPlayerReadyEvent.value = Event(true)
                    Timber.d("Player is ready to play")
                }
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


    val nextTrackEvent = Variable(Event(false))
    val errorEvent = Variable(Event(false))
    val isPlayerReadyEvent = Variable(Event(false))

    fun createPlayer(){
        Timber.d("Created")
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()).apply {
            addListener(playerEventListener)
            Timber.d("Listener attached")
        }
        Timber.d("Player created")
    }

    fun isPlayerInitialized() = exoPlayer != null



    private fun shouldPreparePlayerAgain(playWhenReady: Boolean, playbackState: Int)
            = playWhenReady && (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED)

    private fun preparePlayer(bookId:String,trackList: List<AudioPlayListItem>) {

        Timber.d("Preparing player")
        exoPlayer?.prepare(prepareMediaHandler.createMediaSource(bookId, trackList))
        exoPlayer?.seekTo(trackPos, C.TIME_UNSET)
    }

    fun stopIfPlaying(){

        exoPlayer?.let {
            if(it.isPlaying){
                it.stop()
                Timber.d("Player stopped")
            }
        }
    }

    fun isPlaying():Boolean{
        return exoPlayer?.isPlaying?:false
    }

    fun playFromPosition(pos:Int){
        exoPlayer?.let {
            it.seekTo(pos, C.TIME_UNSET)
            Timber.d("Seek is set to $pos")
            it.playWhenReady = true

            _trackTitle.value = trackList[it.currentWindowIndex].title?:"NA"
            Timber.d("Track title updated and soon will be played")
        }
    }

    fun getTimeLeftInMillis():Long{
        exoPlayer?.let {
            val timeLeft = it.duration - it.currentPosition
            return if(timeLeft>0) timeLeft else 0
        }

        return 0
    }

    fun getProgressInPercent():Int{
        exoPlayer?.let {
            return (it.currentPosition*100/it.duration).toInt()
        }
        return 0
    }

    fun getCurrentPlayingTrackPosition():Int{
        var ret = 0
        exoPlayer?.let {
            ret =  it.currentWindowIndex
        }

        Timber.d("Current track pos is $ret")
        return ret
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
            playWhenReady = true

            if(playbackError!=null){
                Timber.d("Retrying ")
                retry()
            }
        }
    }

    fun destroyPlayer(){
        Timber.i("Destroyed")
        exoPlayer?.run {
            removeListener(playerEventListener)
            release()
            Timber.d("Release player")
        }

        exoPlayer = null
    }

    fun bookDetails(bookId: String?,trackList: List<AudioPlayListItem>?){

        bookId?.let {
            this.bookId = bookId
        }

        trackList?.let {
            this.trackList = trackList
        }
    }
}