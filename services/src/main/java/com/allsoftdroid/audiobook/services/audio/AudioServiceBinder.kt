package com.allsoftdroid.audiobook.services.audio

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.PowerManager
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.Utility.Utils
import com.allsoftdroid.common.base.extension.AudioPlayListItem


class AudioServiceBinder(application: Application) : Binder(),MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    // Check if stream audio.
    private var streamAudio = true

    // Media player that play audio.
    private var audioPlayer: MediaPlayer? = null

    // Caller activity context, used when play local audio file.
    private var context: Context = application

    //id of playing audio book
    private lateinit var bookId:String

    private var isPlayerInitialized = false

    companion object {
        //song list
        private lateinit var trackList: List<AudioPlayListItem>
        //current position
    }

    private var trackPos:Int = 0

    private var _trackTitle = MutableLiveData<String>()
    val trackTitle : LiveData<String>
        get() = _trackTitle



    fun setContext(context: Context) {
        this.context = context
    }

    private fun getContext() = context

    fun isStreamAudio(): Boolean {
        return streamAudio
    }

    fun isPlaying() = audioPlayer?.isPlaying?:false

    fun setStreamAudio(streamAudio: Boolean) {
        this.streamAudio = streamAudio
    }

    private fun setTrackPosition(pos:Int){
        trackPos = pos
    }

    private fun setBookId(id:String){
        bookId = id
    }

    fun setMultipleTracks(tracks: List<AudioPlayListItem>){
        trackList = tracks
    }

    fun getCurrentTrackTitle() = trackTitle


    // Pause playing audio.
    fun pauseAudio() {
        if (audioPlayer != null) {
            audioPlayer!!.pause()
        }
    }

    // Stop play audio.
    fun stopAudio() {
        if (audioPlayer != null) {
            audioPlayer!!.stop()
        }
    }

    fun initializeAndPlay(id : String,pos : Int=0){

        if(!isPlayerInitialized){
            initAudioPlayer()
            isPlayerInitialized = true
        }

        if(isStreamAudio()){
            stopAudio()
        }

        setBookId(id)
        setTrackPosition(pos)
        playTrack()
    }

    private fun initAudioPlayer(){
        audioPlayer = MediaPlayer()

        audioPlayer?.let {

            it.setWakeMode(getContext(),
                PowerManager.PARTIAL_WAKE_LOCK)
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            //set listeners
            it.setOnPreparedListener(this)
            it.setOnCompletionListener(this)
            it.setOnErrorListener(this)
        }
    }

    private fun playTrack(){
        audioPlayer?.let {player ->
            player.reset()

            val track = trackList[trackPos]
            _trackTitle.value = track.title?:"UNKNOWN"

            val filePath = Utils.getRemoteFilePath(track.filename,bookId)

            if (!TextUtils.isEmpty(filePath)) {
                if (isStreamAudio()) {
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                player.setDataSource(filePath)
            } else {
                player.setDataSource(getContext()!!, Uri.parse(filePath))
            }

            player.prepareAsync()

        }
    }

    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
        player?.reset()

        return false
    }

    override fun onCompletion(player: MediaPlayer?) {
        player?.let {
            if (player.currentPosition>0){
                player.reset()
                playNext()
            }
        }
    }

    //skip to previous track
    fun playPrev() {

        trackPos = (trackPos-1)% trackList.size
        playTrack()
    }

    //skip to next
    fun playNext() {

        trackPos = (trackPos+1)% trackList.size
        playTrack()
    }


    // Destroy audio player.
    private fun destroyAudioPlayer() {
        if (audioPlayer != null) {
            if (audioPlayer!!.isPlaying) {
                audioPlayer!!.stop()
            }

            audioPlayer!!.release()

            audioPlayer = null

            isPlayerInitialized = false
        }
    }

    // Return current audio play position.
    fun getCurrentAudioPosition(): Int {
        var ret = 0
        if (audioPlayer != null) {
            ret = audioPlayer!!.currentPosition
        }
        return ret
    }

    // Return total audio file duration.
    private fun getTotalAudioDuration(): Int {
        var ret = 0
        if (audioPlayer != null) {
            ret = audioPlayer!!.duration
        }

        return ret
    }

    // Return current audio player progress value.
    fun getAudioProgress(): Int {
        var ret = 0
        val currAudioPosition = getCurrentAudioPosition()
        val totalAudioDuration = getTotalAudioDuration()
        if (totalAudioDuration > 0) {
            ret = currAudioPosition * 100 / totalAudioDuration
        }
        return ret
    }


}
