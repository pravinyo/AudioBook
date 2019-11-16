package com.allsoftdroid.audiobook.presentation

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModelFactory
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ConnectivityReceiver
import com.allsoftdroid.common.base.store.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class MainActivity : BaseActivity(),ConnectivityReceiver.ConnectivityReceiverListener {

    override val layoutResId = R.layout.activity_main

    companion object{
        const val MINI_PLAYER_TAG = "MiniPlayer"
    }

    private val mainActivityViewModel : MainActivityViewModel by lazy {

        ViewModelProviders.of(this,MainActivityViewModelFactory(application))
            .get(MainActivityViewModel::class.java)
    }

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventBus.getEventBusInstance()
    }

    private val audioManager : AudioManager by lazy {
        val activity = requireNotNull(this) {
            "You can only access the booksViewModel after onCreated()"
        }

        AudioManager.getInstance(activity.applicationContext)
    }

    private val snackbar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    private val connectionListener by lazy {
        ConnectivityReceiver()
    }

    private lateinit var disposable : Disposable

    override fun onStart() {
        super.onStart()

        audioManager.bindAudioService()
        registerReceiver(connectionListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        Timber.d("Main Activity  start")
        mainActivityViewModel.showPlayer.observe(this, Observer {
            it.getContentIfNotHandled()?.let { shouldShow ->

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
                audioManager.playNext()
                eventStore.publish(Event(TrackDetails(trackTitle = audioManager.getTrackTitle()?:"UNKNOWN",bookId = audioManager.getBookId())))
                Timber.d("Next event occurred")
            }

            is Previous -> {
                audioManager.playPrevious()
                eventStore.publish(Event(TrackDetails(trackTitle = audioManager.getTrackTitle()?:"UNKNOWN",bookId = audioManager.getBookId())))
                Timber.d("Previous event occur")
            }

            is Play -> {
                audioManager.playTrack()
                Timber.d("Play track event")
            }

            is Pause -> {
                audioManager.playPrevious()
                Timber.d("Play previous event")
            }

            is PlaySelectedTrack -> {
                audioManager.setPlayTrackList(event.trackList,event.bookId)
                audioManager.playTrackAtPosition(event.position)
                eventStore.publish(Event(TrackDetails(trackTitle = audioManager.getTrackTitle()?:"UNKNOWN",bookId = audioManager.getBookId())))
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
            snackbar.show()
        } else {
            snackbar.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        audioManager.unBoundAudioService()
        unregisterReceiver(connectionListener)
    }
}
