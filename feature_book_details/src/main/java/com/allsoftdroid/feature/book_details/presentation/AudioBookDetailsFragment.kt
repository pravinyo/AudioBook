package com.allsoftdroid.feature.book_details.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import com.allsoftdroid.common.base.network.StoreUtils
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.downloader.*
import com.allsoftdroid.common.base.store.userAction.OpenDownloadUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import com.allsoftdroid.common.base.utils.ShareUtils
import com.allsoftdroid.common.base.utils.StoragePermissionHandler
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding
import com.allsoftdroid.feature.book_details.di.BookDetailsModule
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.AudioBookTrackAdapter
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.DownloadItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.ProgressbarItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import com.allsoftdroid.feature.book_details.utils.*
import com.allsoftdroid.feature.book_details.utils.TextFormatter.formattedBookDetails
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.util.*


class AudioBookDetailsFragment : BaseUIFragment(),KoinComponent {


    private lateinit var argBookId : String
    private lateinit var argBookTitle :String
    private lateinit var argBookName: String
    private var argBookTrackNumber:Int = 0

    /**
    Lazily initialize the view model
     */
    private val bookDetailsViewModel: BookDetailsViewModel by viewModel{
        parametersOf(Bundle(), "vm_audio_book_details")
    }
    private val eventStore : AudioPlayerEventStore by inject()
    private val downloadStore : DownloadEventStore by inject()
    private val userActionStore : UserActionEventStore by inject()
    private val disposable : CompositeDisposable = CompositeDisposable()
    private lateinit var dataBindingReference : FragmentAudiobookDetailsBinding

    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null
    private var mDescriptionLoadingAnimation:ViewLoadingAnimation?=null

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

        setupUI(dataBinding)
        setupEventListener(dataBinding)
        configureBackdrop(dataBinding)

        dataBindingReference = dataBinding

        ViewCompat.setTranslationZ(dataBinding.root, 0f)
        return dataBinding.root
    }

    private fun configureBackdrop(dataBinding:FragmentAudiobookDetailsBinding) {
        // Get the fragment reference
        val fragment = this.childFragmentManager.findFragmentById(R.id.filter_fragment) as BackDropFragment

        fragment.setSharedViewModel(bookDetailsViewModel)

        fragment.let {
            it.view?.let {view ->
                // Get the BottomSheetBehavior from the fragment view
                BottomSheetBehavior.from(view).let { bsb ->
                    // Set the initial state of the BottomSheetBehavior to HIDDEN
                    bsb.state = BottomSheetBehavior.STATE_HIDDEN

                    // Set the trigger that will expand your view
                    dataBinding.imgViewBookInfo.setOnClickListener {
                        Timber.d("Info button clicked")
                        bsb.state = BottomSheetBehavior.STATE_EXPANDED
                    }

                    Timber.d("Fragment filter listener attached")
                    // Set the reference into class attribute (will be used latter)
                    mBottomSheetBehavior = bsb
                }
            }?:Timber.d("Fragment filter view is null")
        }
    }

    private fun setupEventListener(dataBinding: FragmentAudiobookDetailsBinding) {
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

        bookDetailsViewModel.networkResponse.observe(viewLifecycleOwner, Observer {
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
                    NetworkState.CONNECTION_ERROR -> {
                        setVisibility(dataBinding.pbContentLoading,set = false)

                        if(bookDetailsViewModel.audioBookTracks.value.isNullOrEmpty()){
                            setVisibility(dataBinding.networkNoConnection,set = true)
                        }
                        Toast.makeText(activity,getString(R.string.connection_error_message),Toast.LENGTH_SHORT).show()
                    }
                    NetworkState.SERVER_ERROR -> {
                        setVisibility(dataBinding.pbContentLoading,set = false)

                        if(bookDetailsViewModel.audioBookTracks.value.isNullOrEmpty()){
                            setVisibility(dataBinding.serverError,set = true)
                        }
                        Toast.makeText(activity,getString(R.string.server_error_message),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        bookDetailsViewModel.backArrowPressed.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                onBackPressed()
            }
        })
    }

    private fun setupUI(dataBinding: FragmentAudiobookDetailsBinding) {

        mDescriptionLoadingAnimation = ViewLoadingAnimation(dataBinding.textViewBookIntro)
        mDescriptionLoadingAnimation?.colorize()

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
            DownloadItemClickedListener { trackId, downloadStatus ->

                when(downloadStatus){
                    is DOWNLOADING,is PROGRESS -> {
                        userActionStore.publish(Event(OpenDownloadUI(this::class.java.simpleName)))
                    }

                    is NOTHING,is CANCELLED -> {
                        bookDetailsViewModel.downloadSelectedItemWith(trackId)
                        Timber.d("Download Track with $trackId")
                    }

                    else -> Timber.d("Ignored $trackId having download status $downloadStatus")
                }
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

            removeLoading()
            mDescriptionLoadingAnimation?.stop()
        })

        bookDetailsViewModel.isAddedToListenLater.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { isAdded ->
                dataBinding.imgViewBookListenLater.apply {
                    setImageResource(when(isAdded){
                        true -> R.drawable.ic_clock
                        else -> R.drawable.ic_clock_outline
                    })
                }
            }
        })

        dataBinding.imgViewBookListenLater.setOnClickListener {
            bookDetailsViewModel.isAddedToListenLater.value?.let {
                if(it.peekContent()){
                    bookDetailsViewModel.removeFromListenLater()
                }else{
                    bookDetailsViewModel.addToListenLater()
                }
            }
        }

        dataBinding.imgViewBookShare.setOnClickListener {

            bookDetailsViewModel.audioBookMetadata.value?.let {
                ShareUtils.share(
                    context = this.requireActivity(),
                    subject = "${it.title} on AudioBook",
                    txt = "Listen ${it.title} written by '${it.creator}' on AudioBook App," +
                            " Start Listening: ${StoreUtils.getStoreUrl(requireActivity())}"
                )
            }
        }

        dataBinding.imgViewBookDownload.setOnClickListener {
            if(StoragePermissionHandler.isPermissionGranted(requireActivity())){
                val isSent = bookDetailsViewModel.downloadAllChapters()
                if(!isSent){
                    Toast.makeText(requireActivity(),getText(R.string.download_soon_start),Toast.LENGTH_SHORT).show()
                }else{
                    dataBinding.imgViewBookDownload.setBackgroundResource(R.drawable.gradiant_background)
                }

            }else{
                StoragePermissionHandler.requestPermission(requireActivity())
            }
        }

        dataBinding.bookMediaActionsPlay.setOnClickListener {
            resumeOrPlayFromStart()
        }

        dataBinding.bookMediaActionsRead.setOnClickListener {
            bookDetailsViewModel.additionalBookDetails.value?.let {
                if(it.gutenbergUrl.isNotEmpty() && isLinkValid(it.gutenbergUrl)){
                    val uri  = Uri.parse(it.gutenbergUrl)
                    val intent = Intent(Intent.ACTION_VIEW,uri)
                    startActivity(Intent.createChooser(intent,getString(R.string.open_with_label)))
                }else if (it.archiveUrl.isNotEmpty() && isLinkValid(it.archiveUrl)){
                    val uri  = Uri.parse(it.archiveUrl)
                    val intent = Intent(Intent.ACTION_VIEW,uri)
                    startActivity(Intent.createChooser(intent,getString(R.string.open_with_label)))
                }else{
                    Toast.makeText(this.requireActivity(),getString(R.string.no_link_found),Toast.LENGTH_SHORT).show()
                }
            }?:Toast.makeText(this.requireActivity(),getString(R.string.wait_message),Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLinkValid(archiveUrl: String): Boolean {
        if (archiveUrl.toLowerCase(Locale.ROOT).contains(getString(R.string.archive_domain)))
            return true

        if (archiveUrl.toLowerCase(Locale.ROOT).contains(getString(R.string.gutenberg_domain)))
            return true

        return false
    }

    private fun resumeOrPlayFromStart() {
        bookDetailsViewModel.audioBookMetadata.value?.let {
            playSelectedTrackFile(bookDetailsViewModel.getCurrentPlayingTrack(),it.title)
        }
    }

    private fun removeLoading() {
        setVisibility(dataBindingReference.networkNoConnection,set = false)
        setVisibility(dataBindingReference.pbContentLoading,set = false)
    }

    private fun handleDownloaderEvent(event: Event<DownloadEvent>) {
        event.peekContent().let {
            if(it is DownloadNothing || it is PullAndUpdateStatus || it is MultiDownload) return

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
                        if(event.trackList.isNotEmpty()){
                            dataBindingReference.tvToolbarTitle.apply {
                                text = getNormalizedText(text = event.trackList[event.position-1].title,limit = 30)
                            }
                            bookDetailsViewModel.onPlayItemClicked(event.position)
                        }

                    }

                    is TrackDetails -> {
                        dataBindingReference.tvToolbarTitle.apply {
                            text = getNormalizedText(text = event.trackTitle,limit = 30)
                        }
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

                    is Finished -> {
                        Timber.d("Finished event received: Resetting list")
                        bookDetailsViewModel.resetUI()
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

        }?:Toast.makeText(this.context,getString(R.string.track_not_available_message),Toast.LENGTH_SHORT).show()
    }

    private fun setVisibility(view:View,set:Boolean){
         view.visibility = if (set) View.VISIBLE else View.GONE
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        // With the reference of the BottomSheetBehavior stored
        if(mBottomSheetBehavior == null){
            callback.isEnabled = false
            this.requireActivity().onBackPressed()
        }else{
            mBottomSheetBehavior?.let {
                if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                    it.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    callback.isEnabled = false
                    this.requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}