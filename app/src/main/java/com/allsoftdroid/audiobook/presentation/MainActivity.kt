package com.allsoftdroid.audiobook.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.di.AppModule
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.utility.MovableFrameLayout
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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

    private val connectionListener: ConnectionLiveData by inject{parametersOf(this)}



    private val snackBar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppModule.injectFeature()

    }

    override fun onStart() {
        super.onStart()

        mainActivityViewModel.bindAudioService()

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

        mainActivityViewModel.playerEvent.observe(this, Observer {
            it.getContentIfNotHandled()?.let {audioPlayerEvent ->
                Timber.d("Event is new and is being handled")

                connectionListener.value?.let { isConnected ->
                    if(!isConnected) Toast.makeText(this,"Please Connect to Internet",Toast.LENGTH_SHORT).show()
                    performAction(audioPlayerEvent)
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
                findViewById<MovableFrameLayout>(R.id.miniPlayerContainer).visibility = View.VISIBLE

                findViewById<View>(R.id.navHostFragment).apply {

                    val layout = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,CoordinatorLayout.LayoutParams.MATCH_PARENT)
                    layout.bottomMargin = resources.getDimension(R.dimen.mini_player_height).toInt()

                    layoutParams = layout
                }
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

    private fun performAction(event: AudioPlayerEvent){
        when(event){
            is Next -> {
                mainActivityViewModel.nextTrack(event)
            }

            is Previous -> {
                mainActivityViewModel.previousTrack(event)
            }

            is Play -> {
                mainActivityViewModel.resumeOrPlayTrack()
            }

            is Pause -> {
                mainActivityViewModel.pauseTrack()
            }

            is PlaySelectedTrack -> {

                mainActivityViewModel.apply {
                    playSelectedTrack(event)
                    playerStatus(true)
                }
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
        stopAudioService()
        mainActivityViewModel.clearSharedPref()
    }

    private fun stopAudioService(){
        try{
            mainActivityViewModel.unBoundAudioService()
        }catch (exception: Exception){
            Timber.d(exception)
        }
    }
}
