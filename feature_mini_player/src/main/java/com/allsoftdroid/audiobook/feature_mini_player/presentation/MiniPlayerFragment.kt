package com.allsoftdroid.audiobook.feature_mini_player.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.allsoftdroid.audiobook.feature_mini_player.R
import com.allsoftdroid.audiobook.feature_mini_player.databinding.FragmentMiniPlayerBinding
import com.allsoftdroid.audiobook.feature_mini_player.di.injectFeature
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModel
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.*
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import timber.log.Timber

class MiniPlayerFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private val miniPlayerViewModel: MiniPlayerViewModel by inject()

    private val eventStore : AudioPlayerEventStore by inject()

    private var currentPlayingIndex = 0

    private lateinit var dispose : Disposable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("Mini Player fragment created")
        val binding : FragmentMiniPlayerBinding = inflateLayout(inflater,R.layout.fragment_mini_player,container)

        injectFeature()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = miniPlayerViewModel


        miniPlayerViewModel.nextTrackClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { nextClicked ->
                if(nextClicked){
                    Timber.d("Sending new next event")
                    eventStore.publish(Event(Next(PlayingState(
                        playingItemIndex = currentPlayingIndex+1,
                        action_need = true
                    ))))
                }
            }
        })

        miniPlayerViewModel.previousTrackClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                previousClicked ->

                if(previousClicked){
                    Timber.d("Sending new Previous event")
                    eventStore.publish(Event(Previous(PlayingState(
                        playingItemIndex = currentPlayingIndex-1,
                        action_need = true
                    ))))
                }
            }
        })

        miniPlayerViewModel.playPausedClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                    shouldPlay ->

                Timber.d("should play event is $shouldPlay")

                if(shouldPlay){
                    Timber.d("Sending new play event")
                    eventStore.publish(Event(Play(PlayingState(
                        playingItemIndex = currentPlayingIndex,
                        action_need = true
                    ))))
                }else{
                    Timber.d("Sending new pause event")
                    eventStore.publish(Event(Pause(PlayingState(
                        playingItemIndex = currentPlayingIndex,
                        action_need = true
                    ))))
                }
            }
        })

        dispose = eventStore.observe()
            .subscribe {
                Timber.d("Peeking event default")
                it.peekContent().let {event ->
                    when(event){
                        is TrackDetails -> {
                            Timber.d("Received event for update track details event")
                            updateTrackDetails(title = event.trackTitle,bookId = event.bookId)
                            currentPlayingIndex = event.position
                        }

                        is Play -> {
                            miniPlayerViewModel.setShouldPlay(play = true)
                        }

                        is Pause -> {
                            miniPlayerViewModel.setShouldPlay(play = false)
                        }
                    }
                }
            }

        return binding.root
    }

    private fun updateTrackDetails(title:String,bookId:String) {
        miniPlayerViewModel.setTrackTitle(title)
        miniPlayerViewModel.setBookId(bookId)

        Timber.d("State change event sent")
    }
}