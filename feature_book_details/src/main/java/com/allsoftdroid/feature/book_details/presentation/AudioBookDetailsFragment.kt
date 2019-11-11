package com.allsoftdroid.feature.book_details.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.*
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding
import com.allsoftdroid.common.base.utils.PlayerStatusListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.AudioBookTrackAdapter
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class AudioBookDetailsFragment : BaseContainerFragment(){


    private lateinit var bookId : String

    /**
    Lazily initialize the view model
     */
    private val bookDetailsViewModel: BookDetailsViewModel by lazy {

        val activity = requireNotNull(this.activity) {
            "You can only access the booksViewModel after onCreated()"
        }

        ViewModelProviders.of(this, BookDetailsViewModelFactory(activity.application,bookId))
            .get(BookDetailsViewModel::class.java)
    }

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventStore.getInstance(Event(Play("")))
    }

    private lateinit var listener : PlayerStatusListener
    private lateinit var disposable : Disposable

    private lateinit var dataBindingReference : FragmentAudiobookDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding : FragmentAudiobookDetailsBinding = inflateLayout(inflater,R.layout.fragment_audiobook_details,container)

        bookId = arguments?.getString("bookId")?:""

        dataBinding.lifecycleOwner = viewLifecycleOwner
        dataBinding.audioBookDetailsViewModel = bookDetailsViewModel

        bookDetailsViewModel.backArrowPressed.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                this.findNavController()
                    .navigate(R.id.action_AudioBookDetailsFragment_to_AudioBookListFragment)
            }
        })

        val trackAdapter = AudioBookTrackAdapter(TrackItemClickedListener{ trackNumber,filename,title ->
            trackNumber?.let {
                playSelectedTrackFile(it)

                listener.onPlayerStatusChange(shouldShow = Event(true))
                Timber.d("State change event sent")
            }
        })

        dataBinding.recyclerView.adapter = trackAdapter

        dataBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }

        bookDetailsViewModel.audioBookTracks.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d("list size received is ${it.size}")
                trackAdapter.submitList(null)
                trackAdapter.submitList(it)
            }
        })

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleEvent(it)
            }

        dataBindingReference = dataBinding

        return dataBinding.root
    }

    private fun handleEvent(event: Event<AudioPlayerEvent>) {
        activity?.let {
            event.peekContent().let {event->
                when(event){
                    is Next -> bookDetailsViewModel.updateNextTrackPlaying()
                    is Previous -> bookDetailsViewModel.updatePreviousTrackPlaying()

                    is PlaySelectedTrack -> {
                        dataBindingReference.tvToolbarTitle.text = event.trackList[event.position-1].title
                        bookDetailsViewModel.onPlayItemClicked(event.position)
                    }

                    is Play -> Toast.makeText(it.applicationContext,"Play Pressed Details Fragment",Toast.LENGTH_SHORT).show()
                    is Pause -> Toast.makeText(it.applicationContext,"Pause Pressed Details Fragment",Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(it.applicationContext,"Unknown Pressed Details Fragment",Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun playSelectedTrackFile(currentPos:Int) {
        bookDetailsViewModel.audioBookTracks.value?.let {
            eventStore.publish(Event(PlaySelectedTrack(trackList = it,bookId = bookId,position = currentPos)))
        }?:Toast.makeText(this.context,"Track is not available",Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}