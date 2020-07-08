package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.R
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.databinding.LayoutMainFragmentBinding
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.utils.OnSwipeTouchListener
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.audioPlayer.*
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback
    private val mainPlayerViewModel: MainPlayerViewModel by inject()
    private val eventStore : AudioPlayerEventStore by inject()

    private lateinit var compositeDisposable : CompositeDisposable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        FeatureMainPlayerModule.injectFeature()
        val binding:LayoutMainFragmentBinding = inflateLayout(inflater,
            R.layout.layout_main_fragment,container)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mainPlayerViewModel

        try{
            val isPlaying=requireArguments().getBoolean("isPlaying")

            mainPlayerViewModel.setBookDetails(
                bookId= requireArguments().getString("bookId")!!,
                bookName = requireArguments().getString("bookTitle")?:"NA",
                trackName = requireArguments().getString("trackName")?:"NA",
                currentPlayingTrack = requireArguments().getInt("chapterIndex"),
                totalChapter = requireArguments().getInt("totalChapter"),
                isPlaying = isPlaying)

            if(isPlaying){
                Timber.d("Setting playing control to show pause button")
                mainPlayerViewModel.playPause()
            }else{
                Timber.d("isPlaying is false")
            }

        }catch (e:Exception){
            Toast.makeText(this.activity,"Error in initialization",Toast.LENGTH_SHORT).show()
        }

        binding.toolbarBackButton.setOnClickListener {
            handleBackPressEvent()
        }

        binding.parentContainer.let {layout ->
            layout.setOnTouchListener(object : OnSwipeTouchListener(layout.context) {

                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    handleBackPressEvent()
                }
            })
        }

        compositeDisposable.add(eventStore.observe()
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
                        }

                        is Pause -> {
                            binding.ivBookChapterPlaypause.apply {
                                setImageResource(R.drawable.play_circle_green)
                            }
                        }

                        is Buffering -> {
                            playerState(binding,ready = false)
                        }

                        is Finished -> {
                            mainPlayerViewModel.bookFinished()
                        }

                        is AudioPlayerPlayingState -> {
                            playerState(binding,ready = event.isReady)
                        }
                        else -> Timber.d("Ignore event $event")
                    }
                }
            })

        mainPlayerViewModel.trackProgress.observe(viewLifecycleOwner, Observer {
            binding.pbBookChapterProgress.apply {
                this.progress = it
            }

            Timber.d("Progress received is $it")
        })

        mainPlayerViewModel.trackRemainingTime.observe(viewLifecycleOwner, Observer {

            if (!mainPlayerViewModel.isPlayerBusy()){
                binding.tvBookProgressTime.apply {
                    text = it
                }
            }else{
                binding.tvBookProgressTime.apply {
                    text = getText(R.string.buffer_text)
                }
            }

            Timber.d("Remaining time received is $it")
        })

        ViewCompat.setTranslationZ(binding.root, 100f)
        return binding.root
    }

    private fun playerState(binding: LayoutMainFragmentBinding, ready: Boolean) {
        if (ready){
            binding.ivBookChapterPlaypause.visibility = View.VISIBLE
            binding.pbBookChapterBufferProgress.visibility = View.GONE

            mainPlayerViewModel.setPlayerBusy(status = false)
        }else{
            binding.ivBookChapterPlaypause.visibility = View.GONE
            binding.pbBookChapterBufferProgress.visibility = View.VISIBLE

            mainPlayerViewModel.setPlayerBusy(status = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            handleBackPressEvent()
        }
        compositeDisposable = CompositeDisposable()
        callback.isEnabled = true
    }

    private fun handleBackPressEvent(){
        this.findNavController().navigateUp()
        mainPlayerViewModel.showMiniPlayerIfPlaying()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    override fun onPause() {
        super.onPause()
        mainPlayerViewModel.stopProgressTracking()
    }

    override fun onResume() {
        super.onResume()
        mainPlayerViewModel.resumeProgressTracking()
    }
}