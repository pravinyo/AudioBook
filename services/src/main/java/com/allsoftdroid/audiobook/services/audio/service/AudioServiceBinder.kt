package com.allsoftdroid.audiobook.services.audio.service

import android.app.Application
import android.os.Binder
import com.allsoftdroid.audiobook.services.audio.utils.AudioBookPlayer
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class AudioServiceBinder(application: Application) : Binder(){

    private val player = AudioBookPlayer(application)

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

    val nextTrackEvent = Variable(Event(false))
    val errorEvent = Variable(Event(false))
    val isPlayerReadyEvent = Variable(Event(false))

    private var disposable:CompositeDisposable = CompositeDisposable()

    private fun onCreate(){
        player.createPlayer()
        disposable.add(player.trackTitle.observable.subscribe {
            _trackTitle.value = it
        })

        disposable.add(player.nextTrackEvent.observable.subscribe {
            nextTrackEvent.value = it
        })

        disposable.add(player.errorEvent.observable.subscribe {
            errorEvent.value = it
        })

        disposable.add(player.isPlayerReadyEvent.observable.subscribe{
            isPlayerReadyEvent.value = it
        })
    }

    fun onUnbind() {
        player.destroyPlayer()
        disposable.dispose()
    }

    fun initializeAndPlay(currentPos: Int) {
        if (!player.isPlayerInitialized()){
            Timber.d("Player is not yet created. starting to create")
            onCreate()
        }
        Timber.d("List size is: ${trackList.size} and play item at pos:$currentPos")
        setTrackPosition(currentPos)
        Timber.d("Track pos is set.")
    }

    fun goToPreviousOrBeginning() {
        if(player.isPlayerInitialized()){
            Timber.d("Previous called")
            player.goToPreviousOrBeginning()
        }
    }

    fun goToNext() {
        if(player.isPlayerInitialized()){
            Timber.d("Next called")
            player.goToNext()
        }
    }

    fun pause(){
        if(player.isPlayerInitialized()){
            Timber.d("Player pause")
            player.pause()
        }
    }

    fun resume(){
        if(player.isPlayerInitialized()){
            Timber.d("Player resumed")
            player.resume()
        }
    }

    private fun setTrackPosition(pos:Int){
        trackPos = pos

        if(player.isPlayerInitialized()){
            player.stopIfPlaying()
            player.playFromPosition(pos)
        }
    }

    fun setBookDetails(id: String, name: String){
        bookId = id
        bookName = name
        player.bookDetails(bookId, null)

        Timber.d("Book ID is $id")
    }

    fun setMultipleTracks(tracks: List<AudioPlayListItem>){
        trackList = tracks
        player.bookDetails(null, tracks)
        Timber.d("Track list set and it's size is ${tracks.size}")
    }

    fun getCurrentTrackTitle(): String = trackTitle.value

    // Return current audio play position.
    fun getCurrentAudioPosition(): Int {
        return player.getCurrentPlayingTrackPosition()
    }

    fun getBookId() = bookId
    fun getBookName() = bookName
    fun isInitialized() = player.isPlayerInitialized()

    fun getTrackPlayingProgress():Int{
        return player.getProgressInPercent()
    }

    fun getTrackDurationLeft():Long{
        return player.getTimeLeftInMillis()
    }

    fun isPlaying() = player.isPlaying()
}
