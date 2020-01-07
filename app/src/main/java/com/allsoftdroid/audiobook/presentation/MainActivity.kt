package com.allsoftdroid.audiobook.presentation

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.di.AppModule
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main

    companion object{
        const val MINI_PLAYER_TAG = "MiniPlayer"
    }

    private val mainActivityViewModel : MainActivityViewModel by viewModel()

    private val eventStore : AudioPlayerEventStore by inject()
    private val audioManager : AudioManager by inject()

    private val connectionListener: ConnectionLiveData by inject{parametersOf(this)}

    private lateinit var disposable : Disposable



    private val snackBar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppModule.injectFeature()

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleEvent(it)
            }
    }

    override fun onStart() {
        super.onStart()

        audioManager.bindAudioService()

        connectionListener.observe(this, Observer {isConnected ->
            showNetworkMessage(isConnected)
        })

        Timber.d("Main Activity  start")
        mainActivityViewModel.showPlayer.observe(this, Observer {
            it.peekContent().let { shouldShow ->
                Timber.d("Player state event received from view model")
                miniPlayerViewState(shouldShow)
            }
        })

        mainActivityViewModel.stopServiceEvent.observe(this, Observer {
            it.getContentIfNotHandled()?.let { stopEvent ->
                if(stopEvent){
                    stopAudioService()
                }
            }
        })
    }

    private fun miniPlayerViewState(shouldShow: Boolean) {
        if(shouldShow){

            val fragment = supportFragmentManager.findFragmentByTag(MINI_PLAYER_TAG)

            if(fragment == null){
                supportFragmentManager.beginTransaction()
                    .add(R.id.miniPlayerContainer,MiniPlayerFragment(),MINI_PLAYER_TAG)
                    .commit()
                findViewById<FragmentContainerView>(R.id.miniPlayerContainer).visibility = View.VISIBLE
            }

        }else{
            val fragment = supportFragmentManager.findFragmentByTag(MINI_PLAYER_TAG)

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .hide(it)
                    .commit()
            }

            Toast.makeText(this,"Hide Mini Player",Toast.LENGTH_SHORT).show()
        }

    }

    private fun handleEvent(event: Event<AudioPlayerEvent>) {

        event.getContentIfNotHandled()?.let {audioPlayerEvent ->
            Timber.d("Event is new and is being handled")

            connectionListener.value?.let { isConnected ->
                if(!isConnected) Toast.makeText(this,"Please Connect to Internet",Toast.LENGTH_SHORT).show()
                performAction(audioPlayerEvent)
            }
        }
    }

    private fun performAction(event: AudioPlayerEvent){
        when(event){
            is Next -> {
                val state = event.result as PlayingState

                if(state.action_need) audioManager.playNext()

                eventStore.publish(Event(TrackDetails(
                    trackTitle = audioManager.getTrackTitle(),
                    bookId = audioManager.getBookId(),
                    position = state.playingItemIndex)))

                Timber.d("Next event occurred")
            }

            is Previous -> {

                val state = event.result as PlayingState

                audioManager.playPrevious()

                eventStore.publish(Event(TrackDetails(
                    trackTitle = audioManager.getTrackTitle(),
                    bookId = audioManager.getBookId(),
                    position = state.playingItemIndex)))
                Timber.d("Previous event occur")
            }

            is Play -> {
                audioManager.resumeTrack()
                Timber.d("Play/Resume track event")
            }

            is Pause -> {
                audioManager.pauseTrack()
                Timber.d("pause event")
            }

            is PlaySelectedTrack -> {

                audioManager.setPlayTrackList(event.trackList,event.bookId,event.bookName)
                audioManager.playTrackAtPosition(event.position)

                eventStore.publish(Event(TrackDetails(
                    trackTitle = audioManager.getTrackTitle(),
                    bookId = audioManager.getBookId(),
                    position = event.position)))

                mainActivityViewModel.playerStatus(true)

                Timber.d("Play selected track event")
            }
            else -> {
                Timber.d("Unknown event received")
                Timber.d("Unknown Event has message of type TrackDetails: "+(event is TrackDetails))
                Timber.d("Unknown Event has message of type Initial: "+(event is Initial))
            }
        }
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar.show()
        } else {
            snackBar.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun stopAudioService(){
        try{
            audioManager.unBoundAudioService()
        }catch (exception: Exception){
            Timber.d(exception)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        event?.let {
            when(event.keyCode){
                KeyEvent.KEYCODE_HEADSETHOOK -> {
                    if(audioManager.isPlayerCreated()){
                        if(!audioManager.isPlaying()){
                            Timber.d("Sending new play event")
                            eventStore.publish(Event(Play(PlayingState(
                                playingItemIndex = audioManager.currentPlayingIndex(),
                                action_need = true
                            ))))
                        }else{
                            Timber.d("Sending new pause event")
                            eventStore.publish(Event(Pause(PlayingState(
                                playingItemIndex = audioManager.currentPlayingIndex(),
                                action_need = true
                            ))))
                        }
                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }
}
