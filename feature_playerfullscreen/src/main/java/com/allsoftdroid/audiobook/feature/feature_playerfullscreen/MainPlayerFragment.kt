package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.databinding.LayoutMainFragmentBinding
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.audioPlayer.*
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback
    private val mainPlayerViewModel: MainPlayerViewModel by inject()
    private val eventStore : AudioPlayerEventStore by inject()

    private lateinit var dispose : Disposable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        FeatureMainPlayerModule.injectFeature()
        val binding:LayoutMainFragmentBinding = inflateLayout(inflater,R.layout.layout_main_fragment,container)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mainPlayerViewModel

        binding.toolbarBackButton.setOnClickListener {
            handleBackPressEvent()
        }


        dispose = eventStore.observe()
            .subscribe {
                Timber.d("Peeking event default")
                it.peekContent().let {event ->
                    when(event){
                        is TrackDetails -> {
                            Timber.d("Received event for update track details event")
                            mainPlayerViewModel.setBookDetails(
                                bookId= event.bookId,
                                bookName = "Unknown",
                                trackName = event.trackTitle,
                                currentPlayingTrack = event.position,
                                totalChapter = 0)
                        }

                        is Play, is Next, is Previous -> {
                            mainPlayerViewModel.setShouldPlay(play = true)
                        }

                        is Pause -> {
                            mainPlayerViewModel.setShouldPlay(play = false)
                        }
                    }
                }
            }


        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            handleBackPressEvent()
        }

        callback.isEnabled = true
    }

    private fun handleBackPressEvent(){
        this.findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dispose.dispose()
    }
}