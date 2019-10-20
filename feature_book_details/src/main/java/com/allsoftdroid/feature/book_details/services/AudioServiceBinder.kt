package com.allsoftdroid.feature.book_details.services

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature.book_details.Utility.Utils
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import java.io.IOException


class AudioServiceBinder : Binder(),MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    // Save local audio file uri ( local storage file. ).
    private var audioFileUri: Uri? = null

    // Save web audio file url.
    private var audioFileUrl = ""

    // Check if stream audio.
    private var streamAudio = false

    // Media player that play audio.
    private var audioPlayer: MediaPlayer? = null

    // Caller activity context, used when play local audio file.
    private var context: Context? = null

    // This Handler object is a reference to the caller activity's Handler.
    // In the caller activity's handler, it will update the audio play progress.
    private var audioProgressUpdateHandler: Handler? = null


    //id of playing audio book
    private lateinit var bookId:String

    companion object{
        //song list
        private lateinit var trackList: List<AudioBookTrackDomainModel>
        //current position

        // This is the message signal that inform audio progress updater to update audio progress.
        val UPDATE_AUDIO_PROGRESS_BAR = 1
    }

    private var trackPos:Int = 0

    private var _trackTitle = MutableLiveData<String>()
    val trackTitle : LiveData<String>
        get() = _trackTitle



    fun getContext(): Context? {
        return context
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun getAudioFileUrl(): String {
        return audioFileUrl
    }

    fun setAudioFileUrl(audioFileUrl: String) {
        this.audioFileUrl = audioFileUrl
    }

    fun isStreamAudio(): Boolean {
        return streamAudio
    }

    fun isPlaying() = audioPlayer?.isPlaying?:false

    fun setStreamAudio(streamAudio: Boolean) {
        this.streamAudio = streamAudio
    }

    fun getAudioFileUri(): Uri? {
        return audioFileUri
    }

    fun setTrackPosition(pos:Int){
        trackPos = pos
    }

    private fun setBookId(id:String){
        bookId = id
    }

    fun setMultipleTracks(tracks: List<AudioBookTrackDomainModel>){
        trackList = tracks
    }

    fun setAudioFileUri(audioFileUri: Uri) {
        this.audioFileUri = audioFileUri
    }

    fun getAudioProgressUpdateHandler(): Handler? {
        return audioProgressUpdateHandler
    }

    fun setAudioProgressUpdateHandler(audioProgressUpdateHandler: Handler) {
        this.audioProgressUpdateHandler = audioProgressUpdateHandler
    }

    fun getCurrentTrackTitle() = trackTitle

    // Start play audio.
    fun startAudio() {
        initAudioPlayer()
        if (audioPlayer != null) {
            audioPlayer!!.start()
        }
    }

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
            destroyAudioPlayer()
        }
    }

    fun onCreate(id : String){
        audioPlayer = MediaPlayer()
        trackPos = 0

        bookId = id

        initAudioPlayer2()
    }

    private fun initAudioPlayer2(){
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

    fun playTrack(){
        audioPlayer?.let {player ->
            player.reset()

            val track = trackList[trackPos]
            _trackTitle.value = track.trackTitle?:"UNKNOWN"

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


    // Initialise audio player.
    private fun initAudioPlayer() {
        try {
            if (audioPlayer == null) {
                trackPos = 0
                audioPlayer = MediaPlayer()

                if (!TextUtils.isEmpty(getAudioFileUrl())) {
                    if (isStreamAudio()) {
                        audioPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    }
                    audioPlayer!!.setDataSource(getAudioFileUrl())
                } else {
                    audioPlayer!!.setDataSource(getContext()!!, getAudioFileUri()!!)
                }

                audioPlayer!!.prepare()

                // This thread object will send update audio progress message to caller activity every 1 second.
                val updateAudioProgressThread = object : Thread() {
                    override fun run() {
                        while (true) {
                            // Create update audio progress message.
                            val updateAudioProgressMsg = Message()
                            updateAudioProgressMsg.what = UPDATE_AUDIO_PROGRESS_BAR

                            // Send the message to caller activity's update audio prgressbar Handler object.
                            audioProgressUpdateHandler!!.sendMessage(updateAudioProgressMsg)

                            // Sleep one second.
                            try {
                                Thread.sleep(1000)
                            } catch (ex: InterruptedException) {
                                ex.printStackTrace()
                            }

                        }
                    }
                }
                // Run above thread object.
                updateAudioProgressThread.start()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    // Destroy audio player.
    private fun destroyAudioPlayer() {
        if (audioPlayer != null) {
            if (audioPlayer!!.isPlaying) {
                audioPlayer!!.stop()
            }

            audioPlayer!!.release()

            audioPlayer = null
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
