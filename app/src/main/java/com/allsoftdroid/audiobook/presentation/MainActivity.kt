package com.allsoftdroid.audiobook.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.di.AppModule
import com.allsoftdroid.audiobook.domain.model.LastPlayedTrack
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloaderCore
import com.allsoftdroid.audiobook.feature_downloader.presentation.DownloadManagementActivity
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.utility.MovableFrameLayout
import com.allsoftdroid.audiobook.utility.StoragePermissionHandler
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.downloader.DownloadEvent
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.store.downloader.DownloadNothing
import com.allsoftdroid.common.base.store.downloader.OpenDownloadActivity
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

    private val connectionListener: ConnectionLiveData by inject{parametersOf(this)}

    private val downloadEventStore:DownloadEventStore by inject()

    private val downloader: IDownloaderCore by inject{parametersOf(this)}

    private lateinit var disposable:Disposable



    private val snackBar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppModule.injectFeature()

        mainActivityViewModel.lastPlayed.observe(this, Observer {event ->
            event.getContentIfNotHandled()?.let {
                Timber.d("Last played : ${it.title}")

                val dialog = alertDialog(it)
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }
        })
    }


    private fun alertDialog(lastPlayedTrack: LastPlayedTrack):AlertDialog{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("continue ${lastPlayedTrack.bookName} from where you left,")
        builder.setMessage("Chapter : ${lastPlayedTrack.title}")

        builder.setPositiveButton("Listen") { _, _ ->
            Toast.makeText(this,"Playing",Toast.LENGTH_SHORT).show()
            //Navigate to display page
            val bundle = bundleOf(
                "bookId" to lastPlayedTrack.bookId,
                "title" to lastPlayedTrack.title,
                "trackNumber" to lastPlayedTrack.position)

            connectionListener.value?.let { connected->
                if(connected){
                    Navigation.findNavController(this,R.id.navHostFragment)
                        .navigate(com.allsoftdroid.feature_book.R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle)
                    mainActivityViewModel.clearSharedPref()
                }else{
                    Toast.makeText(this,"Please connect to internet",Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton("Dismiss"){
            _,_ ->
            mainActivityViewModel.clearSharedPref()
        }

        return builder.create()
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

        mainActivityViewModel.playerEvent.observeForever {
            it.getContentIfNotHandled()?.let {audioPlayerEvent ->
                connectionListener.value?.let { isConnected ->
                    Timber.d("Event is new and is being handled")
                    if(!isConnected) Toast.makeText(this,"Please Connect to Internet",Toast.LENGTH_SHORT).show()
                    performAction(audioPlayerEvent)
                }
            }
        }

        disposable = downloadEventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleDownloadEvent(it)
            }
    }

    private fun handleDownloadEvent(event: Event<DownloadEvent>) {

        event.getContentIfNotHandled()?.let {

            if( it is DownloadNothing) return

            if(!StoragePermissionHandler.isPermissionGranted(this)){
                StoragePermissionHandler.requestPermission(this)
            }else{
                when (it) {
                    is OpenDownloadActivity -> {
                        navigateToDownloadManagementActivity()
                    }
                    else -> downloader.handleDownloadEvent(it)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(StoragePermissionHandler.isRequestGrantedFor(requestCode,grantResults)){
            Toast.makeText(this,"Thanks for granting permission",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"This Feature need Storage Permission",Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDownloadManagementActivity() {
        val intent = Intent(this,
            DownloadManagementActivity::class.java)
        startActivity(intent)
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
                Timber.d("Unknown Event has message of type Initial: "+(event is EmptyEvent))
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
        disposable.dispose()
        downloader.Destroy()
    }

    private fun stopAudioService(){
        try{
            mainActivityViewModel.unBoundAudioService()
        }catch (exception: Exception){
            Timber.d(exception)
        }
    }
}
