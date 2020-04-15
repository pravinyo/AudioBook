package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.databinding.LayoutMainFragmentBinding
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.audioPlayer.*
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback
    private val mainPlayerViewModel: MainPlayerViewModel by inject()
    private val eventStore : AudioPlayerEventStore by inject()

    private val audioManager:AudioManager by inject()

    private lateinit var dispose : Disposable
    private lateinit var refBinding: LayoutMainFragmentBinding
    lateinit var mainHandler: Handler
    private val updateTextTask = object : Runnable {
        override fun run() {
            updateProgress()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        FeatureMainPlayerModule.injectFeature()
        val binding:LayoutMainFragmentBinding = inflateLayout(inflater,R.layout.layout_main_fragment,container)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mainPlayerViewModel

        try{
            mainPlayerViewModel.setBookDetails(
                bookId= arguments!!.getString("bookId")!!,
                bookName = arguments!!.getString("bookTitle")?:"NA",
                trackName = arguments!!.getString("trackName")?:"NA",
                currentPlayingTrack = arguments!!.getInt("chapterIndex"),
                totalChapter = arguments!!.getInt("totalChapter"))
        }catch (e:Exception){
            Toast.makeText(this.activity,"Error in initialization",Toast.LENGTH_SHORT).show()
        }

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
                            mainPlayerViewModel.updateTrackDetails(event.position,event.trackTitle)
                        }

                        is Play, is Next, is Previous -> {
                            binding.ivBookChapterPlaypause.apply {
                                setImageResource(R.drawable.pause_circle_green)
                            }
                            mainPlayerViewModel.setShouldPlay(play = true)
                            mainHandler.post(updateTextTask)
                        }

                        is Pause -> {
                            binding.ivBookChapterPlaypause.apply {
                                setImageResource(R.drawable.play_circle_green)
                            }
                            mainPlayerViewModel.setShouldPlay(play = false)
                            mainHandler.removeCallbacks(updateTextTask)
                        }
                        else -> Timber.d("Ignore event $event")
                    }
                }
            }


        refBinding = binding
        mainHandler = Handler(Looper.getMainLooper())

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
        mainPlayerViewModel.showMiniPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dispose.dispose()
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
    }

    private fun updateProgress(){
        refBinding.pbBookChapterProgress.apply {
            this.progress = audioManager.getPlayingTrackProgress()
        }

        refBinding.tvBookProgressTime.apply {
            this.text = audioManager.getTrackRemainingTime()
        }
    }
}