package com.allsoftdroid.audiobook.presentation

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
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
import com.allsoftdroid.common.base.network.ConnectivityReceiver
import com.allsoftdroid.common.base.store.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.lang.Exception


class MainActivity : BaseActivity(),ConnectivityReceiver.ConnectivityReceiverListener {

    override val layoutResId = R.layout.activity_main

    companion object{
        const val MINI_PLAYER_TAG = "MiniPlayer"
    }

    private val mainActivityViewModel : MainActivityViewModel by viewModel()
    private val eventStore : AudioPlayerEventStore by inject()
    private val audioManager : AudioManager by inject()
    private val connectionListener:ConnectivityReceiver by inject()
    private lateinit var disposable : Disposable

    private val snackBar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModule.injectFeature()
    }

    override fun onStart() {
        super.onStart()

        audioManager.bindAudioService()
        registerReceiver(connectionListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        Timber.d("Main Activity  start")
        mainActivityViewModel.showPlayer.observe(this, Observer {
            it.peekContent().let { shouldShow ->
                Timber.d("Player state event received from view model")
                miniPlayerViewState(shouldShow)
            }
        })

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleEvent(it)
            }

        mainActivityViewModel.stopServiceEvent.observeForever {
            it.getContentIfNotHandled()?.let { stopEvent ->
                if(stopEvent){
                    stopAudioService()
                }
            }
        }
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

        event.getContentIfNotHandled()?.let {
            Timber.d("Event is new and is being handled")
            performAction(it)
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

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar.show()
        } else {
            snackBar.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
        unregisterReceiver(connectionListener)
    }

    private fun stopAudioService(){
        try{
            audioManager.unBoundAudioService()
        }catch (exception: Exception){
            Timber.d(exception.message)
        }
    }
}
