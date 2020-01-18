package com.allsoftdroid.feature.book_details.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.common.base.store.*
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding
import com.allsoftdroid.feature.book_details.di.BookDetailsModule
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.AudioBookTrackAdapter
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class AudioBookDetailsFragment : BaseContainerFragment(),KoinComponent {


    private lateinit var bookId : String
    private lateinit var bookTitle :String
    private var bookTrackNumber:Int = 0

    /**
    Lazily initialize the view model
     */
    private val bookDetailsViewModel: BookDetailsViewModel by viewModel{
        parametersOf(Bundle(), "vm1")
    }

    private val eventStore : AudioPlayerEventStore by inject()


    private lateinit var disposable : Disposable

    private lateinit var dataBindingReference : FragmentAudiobookDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding : FragmentAudiobookDetailsBinding = inflateLayout(inflater,R.layout.fragment_audiobook_details,container)

        bookId = arguments?.getString("bookId")?:""
        bookTitle = arguments?.getString("title")?:""
        bookTrackNumber = arguments?.getInt("trackNumber")?:0

        getKoin().setProperty(BookDetailsModule.PROPERTY_BOOK_ID,bookId)
        BookDetailsModule.injectFeature()

        dataBinding.lifecycleOwner = viewLifecycleOwner
        dataBinding.audioBookDetailsViewModel = bookDetailsViewModel

        bookDetailsViewModel.backArrowPressed.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                this.activity?.onBackPressed()
            }
        })

        val trackAdapter = AudioBookTrackAdapter(TrackItemClickedListener{ trackNumber, _, _ ->
            trackNumber?.let {
                playSelectedTrackFile(it,bookDetailsViewModel.audioBookMetadata.value?.title?:"NA")
                Timber.d("State change event sent: new pos:$it")
            }
        })

        dataBinding.recyclerView.adapter = trackAdapter

        dataBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }

        bookDetailsViewModel.audioBookTracks.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d("list size received is ${it.size}")
                if(it.isNotEmpty()){
                    trackAdapter.submitList(it)
                    setVisibility(dataBinding.networkNoConnection,set = false)

                    if(bookTrackNumber>0){
                        playSelectedTrackFile(bookTrackNumber,bookDetailsViewModel.audioBookMetadata.value?.title?:"NA")
                        dataBinding.tvToolbarTitle.text = bookTitle
                        bookTrackNumber=0
                    }
                }
            }
        })

        disposable  = eventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleEvent(it)
            }

        dataBindingReference = dataBinding

        dataBinding.mstbTrackFormat.apply {
            setElements(R.array.track_format_array,bookDetailsViewModel.trackFormatIndex)

            setOnValueChangedListener {
                bookDetailsViewModel.loadTrackWithFormat(it)
                Toast.makeText(activity,"Loading",Toast.LENGTH_SHORT).show()
            }
        }

        bookDetailsViewModel.networkResponse.observe(this, Observer {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){
                    NetworkState.LOADING -> {
                        setVisibility(dataBinding.pbContentLoading,set = true)
                        setVisibility(dataBinding.networkNoConnection,set = false)
                        Timber.d("Loading")}
                    NetworkState.COMPLETED -> {
                        setVisibility(dataBinding.pbContentLoading,set = false)
                        setVisibility(dataBinding.networkNoConnection,set = false)
                        Timber.d("Completed")}
                    NetworkState.ERROR -> {
                        setVisibility(dataBinding.pbContentLoading,set = false)

                        if(bookDetailsViewModel.audioBookTracks.value.isNullOrEmpty()){
                            setVisibility(dataBinding.networkNoConnection,set = true)
                        }
                        Toast.makeText(activity,"Connection Error",Toast.LENGTH_SHORT).show()}
                }
            }
        })

        return dataBinding.root
    }

    private fun handleEvent(event: Event<AudioPlayerEvent>) {
        activity?.let {
            Timber.d("Peeking content by default")
            event.peekContent().let {event->
                when(event){
                    is Next -> {
                        Timber.d("next event received, updating next track playing")
                        bookDetailsViewModel.updateNextTrackPlaying()
                    }

                    is Previous -> {
                        Timber.d("Previous event occurred, updating previous playing event")
                        bookDetailsViewModel.updatePreviousTrackPlaying()
                    }

                    is PlaySelectedTrack -> {
                        Timber.d("Play selected track event occurred, updating ui")
                        dataBindingReference.tvToolbarTitle.text = event.trackList[event.position-1].title
                        bookDetailsViewModel.onPlayItemClicked(event.position)

                    }

                    is TrackDetails -> {
                        dataBindingReference.tvToolbarTitle.text = event.trackTitle
                        bookDetailsViewModel.onPlayItemClicked(event.position)
                    }

                    is Play -> {
                        Timber.d("Play event received")
                    }

                    is Pause -> {
                        Timber.d("Pause event received")
                    }

                    is Initial -> {
                        Timber.d("Initial event received")
                    }
                    else -> Toast.makeText(it.applicationContext,"Unknown Pressed Details Fragment",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playSelectedTrackFile(currentPos:Int,bookName:String) {
        bookDetailsViewModel.audioBookTracks.value?.let {
            Timber.d("Sending new event for play selected track by the user")
            eventStore.publish(Event(PlaySelectedTrack(trackList = it,bookId = bookId,position = currentPos,bookName = bookName)))
            Timber.d("saving current state event of the track")

        }?:Toast.makeText(this.context,"Track is not available",Toast.LENGTH_SHORT).show()
    }

    private fun setVisibility(view:View,set:Boolean){
         view.visibility = if (set) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}