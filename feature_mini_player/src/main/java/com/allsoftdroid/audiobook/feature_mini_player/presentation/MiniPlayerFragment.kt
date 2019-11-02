package com.allsoftdroid.audiobook.feature_mini_player.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.allsoftdroid.audiobook.feature_mini_player.R
import com.allsoftdroid.audiobook.feature_mini_player.databinding.FragmentMiniPlayerBinding
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModel
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModelFactory
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.utils.PlayerStatusListener
import timber.log.Timber

class MiniPlayerFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private val miniPlayerViewModel: MiniPlayerViewModel by lazy {

        val activity = requireNotNull(this.activity) {
            "You can only access the miniPlayerViewModel after onCreated()"
        }

        ViewModelProviders.of(this, MiniPlayerViewModelFactory(activity.application))
            .get(MiniPlayerViewModel::class.java)
    }

    private val audioManager: AudioManager by lazy {
        val activity = requireNotNull(this.activity){
            "You can only access the miniPlayerViewModel after onCreated()"
        }

        AudioManager.getInstance(activity.applicationContext)
    }

    private lateinit var listener : PlayerStatusListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("Mini Player fragment created")
        val binding : FragmentMiniPlayerBinding = inflateLayout(inflater,R.layout.fragment_mini_player,container)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = miniPlayerViewModel


        miniPlayerViewModel.nextTrackClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { nextClicked ->
                if(nextClicked){
                    audioManager.playNext()
                    updateTrackDetails()
                }
            }
        })

        miniPlayerViewModel.previousTrackClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                previousClicked ->

                if(previousClicked){
                    audioManager.playPrevious()
                    updateTrackDetails()
                }
            }
        })

        miniPlayerViewModel.playPausedClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                    shouldPlay ->

                Timber.d("should play is $shouldPlay")
                if(shouldPlay){
                    audioManager.pauseTrack()
                }else{
                    audioManager.playTrack()
                }
            }
        })

        return binding.root
    }

    private fun updateTrackDetails() {
        miniPlayerViewModel.setTrackTitle(audioManager.getTrackTitle())
        miniPlayerViewModel.setBookId(audioManager.getBookId())

        listener.onPlayerStatusChange(shouldShow = Event(true))
        Timber.d("State change event sent")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PlayerStatusListener) {
            Timber.d("Listener attached")
            listener = context
        } else {
            throw ClassCastException(
                "$context must implement ${PlayerStatusListener::class.java.simpleName}."
            )
        }
    }
}