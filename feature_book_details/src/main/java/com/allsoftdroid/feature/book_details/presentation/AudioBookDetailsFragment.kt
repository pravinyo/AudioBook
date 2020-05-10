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
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.downloader.*
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding
import com.allsoftdroid.feature.book_details.di.BookDetailsModule
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.AudioBookTrackAdapter
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.DownloadItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.ProgressbarItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import com.allsoftdroid.feature.book_details.utils.NetworkState
import com.allsoftdroid.feature.book_details.utils.TextFormatter.formattedBookDetails
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class AudioBookDetailsFragment : BaseContainerFragment(),KoinComponent {


    private lateinit var argBookId : String
    private lateinit var argBookTitle :String
    private lateinit var argBookName: String
    private var argBookTrackNumber:Int = 0

    /**
    Lazily initialize the view model
     */
    private val bookDetailsViewModel: BookDetailsViewModel by viewModel{
        parametersOf(Bundle(), "vm1")
    }

    private val eventStore : AudioPlayerEventStore by inject()
    private val downloadStore : DownloadEventStore by inject()


    private val disposable : CompositeDisposable = CompositeDisposable()

    private lateinit var dataBindingReference : FragmentAudiobookDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding : FragmentAudiobookDetailsBinding = inflateLayout(inflater,R.layout.fragment_audiobook_details,container)

        argBookId = arguments?.getString("bookId")?:""
        argBookTitle = arguments?.getString("title")?:""
        argBookTrackNumber = arguments?.getInt("trackNumber")?:0
        argBookName = arguments?.getString("bookName")?:"NA"

        getKoin().setProperty(BookDetailsModule.PROPERTY_BOOK_ID,argBookId)
        BookDetailsModule.injectFeature()

        dataBinding.lifecycleOwner = viewLifecycleOwner
        dataBinding.audioBookDetailsViewModel = bookDetailsViewModel

        bookDetailsViewModel.backArrowPressed.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                this.activity?.onBackPressed()
            }
        })

        val trackAdapter = AudioBookTrackAdapter(
            downloadStore,
            argBookId,
            TrackItemClickedListener{ trackNumber, _, _ ->
                trackNumber?.let {
                    playSelectedTrackFile(it,bookDetailsViewModel.audioBookMetadata.value?.title?:"NA")
                    Timber.d("State change event sent: new pos:$it")
                }?:Timber.d("State change event sent: new pos:$trackNumber")
                }
            ,
            ProgressbarItemClickedListener {trackId ->
                bookDetailsViewModel.openDownloadsScreen(trackId)
            }
            ,
            DownloadItemClickedListener { trackId ->
                bookDetailsViewModel.downloadSelectedItemWith(trackId)
                Timber.d("Download Track with $trackId")
            }
        )

        dataBinding.recyclerView.adapter = trackAdapter

        dataBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }

        bookDetailsViewModel.audioBookTracks.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d("list size received is ${it.size}")
                if(it.isNotEmpty()){
                    trackAdapter.submitList(it)
                    removeLoading()

                    if(argBookTrackNumber>0){
                        Timber.d("Book Track number is $argBookTrackNumber")
                        playSelectedTrackFile(argBookTrackNumber,argBookName)
                        dataBinding.tvToolbarTitle.text = argBookTitle
                        argBookTrackNumber = 0
                    }
                }
            }
        })

        bookDetailsViewModel.additionalBookDetails.observe(viewLifecycleOwner, Observer {bookDetails->
            Timber.d("Book details received: $bookDetails")
            try{
                dataBinding.textViewBookIntro.text = if(bookDetails==null || bookDetails.description.isEmpty()){
                    bookDetailsViewModel.audioBookMetadata.value?.let {
                        formattedBookDetails(it)
                    }
                }else formattedBookDetails(bookDetails)
            }catch ( e:Exception){
                e.printStackTrace()
                bookDetailsViewModel.audioBookMetadata.value?.let {
                    dataBinding.textViewBookIntro.text = formattedBookDetails(it)
                }
            }
        })

        disposable.add(
            eventStore.observe()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    handleEvent(it)
                }
        )

        disposable.add(
            downloadStore.observe()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    handleDownloaderEvent(it)
                }
        )

        dataBindingReference = dataBinding

//        dataBinding.mstbTrackFormat.apply {
//            setElements(R.array.track_format_array,bookDetailsViewModel.trackFormatIndex)
//
//            setOnValueChangedListener {
//                bookDetailsViewModel.loadTrackWithFormat(it)
//                Toast.makeText(activity,"Loading",Toast.LENGTH_SHORT).show()
//            }
//        }

        bookDetailsViewModel.networkResponse.observe(this, Observer {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){
                    NetworkState.LOADING -> {
                        setVisibility(dataBinding.pbContentLoading,set = true)
                        setVisibility(dataBinding.networkNoConnection,set = false)
                        Timber.d("Loading")}
                    NetworkState.COMPLETED -> {
                        removeLoading()
                        setVisibility(dataBindingReference.pbContentLoading,set = false)
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

    private fun removeLoading() {
        setVisibility(dataBindingReference.networkNoConnection,set = false)
//        setVisibility(dataBindingReference.pbContentLoading,set = false)
    }

    private fun handleDownloaderEvent(event: Event<DownloadEvent>) {
        event.peekContent().let {
            if(it is DownloadNothing || it is OpenDownloadActivity || it is PullAndUpdateStatus) return

            Timber.d("Event is for book: ${it.bookId} - chapter:${it.chapterIndex}")
            bookDetailsViewModel.updateDownloadStatus(it)
        }
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

                    is EmptyEvent -> {
                        Timber.d("Initial event received")
                    }
                    else -> Timber.d("Unknown Pressed Details Fragment")
                }
            }
        }
    }

    private fun playSelectedTrackFile(currentPos:Int,bookName:String) {
        bookDetailsViewModel.audioBookTracks.value?.let {
            Timber.d("Sending new event for play selected track by the user")
            eventStore.publish(Event(
                PlaySelectedTrack(
                    trackList = it,
                    bookId = argBookId,
                    position = currentPos,
                    bookName = bookName
                )
            ))

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