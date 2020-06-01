package com.allsoftdroid.audiobook.services.audio.service

import android.os.Binder
import com.allsoftdroid.audiobook.services.audio.utils.AudioBookPlayer
import com.allsoftdroid.audiobook.services.audio.utils.PlayerState
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class AudioServiceBinder(private val player: AudioBookPlayer) : Binder(){

    //id of playing audio book
    private lateinit var bookId:String
    private lateinit var bookName:String

    companion object {
        //song list
        private lateinit var trackList: List<AudioPlayListItem>
    }

    //current position
    private var trackPos:Int = 0

    private var _trackTitle = Variable("")
    val trackTitle : Variable<String>
        get() = _trackTitle

    private var _playerState = Variable(Event(PlayerState.PlayerIdle))
    val playerState : Variable<Event<PlayerState>>
        get() = _playerState

    private var disposable:CompositeDisposable = CompositeDisposable()

    private fun onCreate(){
        player.createPlayer()
        disposable.add(player.trackTitle.observable.subscribe {
            _trackTitle.value = it
        })

        disposable.add(player.playerState.observable.subscribe { state->
            _playerState.value = state
        })
    }

    /**
     * destroy the player and other listener
     */
    fun onUnbind() {
        player.destroyPlayer()
        disposable.dispose()
    }

    /**
     * initialize the player and play with the provided position
     */
    fun initializeAndPlay(currentPos: Int) {
        if (!player.isPlayerInitialized()){
            Timber.d("Player is not yet created. starting to create")
            onCreate()
        }
        Timber.d("List size is: ${trackList.size} and play item at pos:$currentPos")
        setTrackPosition(currentPos)
        Timber.d("Track pos is set.")
    }

    /**
     * play previous if it is already initialized
     */
    fun goToPreviousOrBeginning() {
        if(player.isPlayerInitialized()){
            Timber.d("Previous called")
            player.goToPreviousOrBeginning()
        }
    }

    /**
     * play next  if it is already initialized
     */
    fun goToNext() {
        if(player.isPlayerInitialized()){
            Timber.d("Next called")
            player.goToNext()
        }
    }

    /**
     * Pause player if it is already initialized
     */
    fun pause(){
        if(player.isPlayerInitialized()){
            Timber.d("Player pause")
            player.pause()
        }
    }

    /**
     * Resume player if it is already initialized
     */
    fun resume(){
        if(player.isPlayerInitialized()){
            Timber.d("Player resumed")
            player.resume()
        }
    }

    /**
     * Check the current playing track to new track
     */
    private fun setTrackPosition(pos:Int){
        trackPos = pos

        if(player.isPlayerInitialized()){
            player.stopIfPlaying()
            player.playFromPosition(pos)
        }
    }

    /**
     * Set necessary details required to initialized the audio player service
     * @param id book identifier
     * @param name book name
     */
    fun setBookDetails(id: String, name: String){
        bookId = id
        bookName = name
        player.bookDetails(bookId, null)

        Timber.d("Book ID is $id")
    }

    /**
     * initialized tracks to be played by the player
     * @param tracks list of [AudioPlayListItem]
     */
    fun setMultipleTracks(tracks: List<AudioPlayListItem>){
        trackList = tracks
        player.bookDetails(null, tracks)
        Timber.d("Track list set and it's size is ${tracks.size}")
    }

    /**
     * get title of the current playing track
     */
    fun getCurrentTrackTitle(): String = trackTitle.value

    /**
     * Return current audio play position.
     */
    fun getCurrentAudioPosition(): Int {
        return player.getCurrentPlayingTrackPosition()
    }

    /**
     * get book identifier of the currently playing track
     */
    fun getBookId() = bookId

    /**
     * get book name of the currently playing track
     */
    fun getBookName() = bookName

    /**
     * Check whether player is initialized or not
     */
    fun isInitialized() = player.isPlayerInitialized()

    /**
     * Get the progress currently playing track in 0 to 100 percent
     * @return progress value between 0 and 100 as percentage
     */
    fun getTrackPlayingProgress():Int{
        return player.getProgressInPercent()
    }

    /**
     * Get the time left to completion of the currently playing track
     * @return time in millis left
     */
    fun getTrackDurationLeft():Long{
        return player.getTimeLeftInMillis()
    }

    /**
     * Check whether player is playing
     * @return true if playing
     */
    fun isPlaying() = player.isPlaying()

    /**
     * Rewind the player by millis to left,, if already initialized
     * @param millis time to be shifted
     */
    fun setPlayerRewindBy(millis:Int) {
        if(player.isPlayerInitialized()){
            player.seekByPosition(-1*millis)
        }
    }

    /**
     * Forward the player by millis to right, if already initialized
     * @param millis time to be shifted
     */
    fun setPlayerForwardBy(millis:Int){
        if(player.isPlayerInitialized()){
            player.seekByPosition(millis)
        }
    }
}
