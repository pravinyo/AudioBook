package com.allsoftdroid.audiobook.presentation

import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModelFactory
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.utils.PlayerStatusListener
import timber.log.Timber


class MainActivity : BaseActivity(), PlayerStatusListener {

    override val layoutResId = R.layout.activity_main

    companion object{
        const val MINI_PLAYER_TAG = "MiniPlayer"
    }

    private val mainActivityViewModel : MainActivityViewModel by lazy {

        ViewModelProviders.of(this,MainActivityViewModelFactory(application))
            .get(MainActivityViewModel::class.java)
    }


    override fun onStart() {
        super.onStart()

        Timber.d("Main Activity  start")
        mainActivityViewModel.showPlayer.observe(this, Observer {
            it.getContentIfNotHandled()?.let { shouldShow ->

                Timber.d("Player state event received from view model")
                if(shouldShow){

                    val fragment = supportFragmentManager.findFragmentByTag(MINI_PLAYER_TAG)

                    if(fragment == null){
                        supportFragmentManager.beginTransaction()
                            .add(R.id.miniPlayerContainer,MiniPlayerFragment(),MINI_PLAYER_TAG)
                            .commit()
                    }

                }else{
                    Toast.makeText(this,"Hide Mini Player",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onPlayerStatusChange(shouldShow: Event<Boolean>) {
        shouldShow.getContentIfNotHandled()?.let {
            Timber.d("Player state event received from fragment")
            mainActivityViewModel.playerStatus(it)
        }
    }
}
