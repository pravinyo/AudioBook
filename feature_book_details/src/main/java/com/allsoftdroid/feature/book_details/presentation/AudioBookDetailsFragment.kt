package com.allsoftdroid.feature.book_details.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.AudioBookTrackAdapter
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModelFactory
import com.allsoftdroid.feature.book_details.services.AudioServiceBinder
import timber.log.Timber
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import android.content.Context.BIND_AUTO_CREATE
import com.allsoftdroid.feature.book_details.services.AudioService
import android.content.Intent
import android.widget.Toast


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
    
    
    /**
     *
     * Audio service code start
     */

    var audioServiceBinder : AudioServiceBinder? = null
    var audioProgressUpdateHandler : Handler? = null
//    var backgroundAudioProgress : Float  = 0f


    // This service connection object is the bridge between activity and background service.
    private val serviceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            // Cast and assign background service's onBind method returned iBander object.
                val service = iBinder as AudioServiceBinder
                audioServiceBinder = service
            }

        override fun onServiceDisconnected(componentName: ComponentName) {
            }
        }
    }

    // Bind background service with caller . Then this caller can use
    // background service's AudioServiceBinder instance to invoke related methods.

    private val playIntent by lazy { Intent(this.activity, AudioService::class.java) }

    private fun bindAudioService() {
        if (audioServiceBinder == null) {
            // Below code will invoke serviceConnection's onServiceConnected method.
            context!!.bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)
            context!!.startService(playIntent)
        }
    }

    // Unbound background audio service with caller activity.
    private fun unBoundAudioService() {
        if (audioServiceBinder != null) {
            context!!.unbindService(serviceConnection)
            context!!.stopService(playIntent)
        }
    }

    // Create audio player progressbar updater.
    // This updater is used to update progressbar to reflect audio play process.
//    private fun createAudioProgressbarUpdater() {
//        /* Initialize audio progress handler. */
//        if (audioProgressUpdateHandler == null) {
//            audioProgressUpdateHandler = @SuppressLint("HandlerLeak")
//            object : Handler() {
//                override fun handleMessage(msg: Message) {
//                    // The update process message is sent from AudioServiceBinder class's thread object.
//                    if (msg.what === audioServiceBinder!!.UPDATE_AUDIO_PROGRESS_BAR) {
//
//                        if (audioServiceBinder != null) {
//                            // Calculate the percentage.
//                            val currProgress = (audioServiceBinder as AudioServiceBinder).getAudioProgress()
//
//                            // Update progressbar. Make the value 10 times to show more clear UI change.
//                            backgroundAudioProgress = currProgress * 10f
//                            Timber.d("Progress: $backgroundAudioProgress")
//                        }
//                    }
//                }
//            }
//        }
//    }
    /**
     *
     * Audio Service code end
     */

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
                bookDetailsViewModel.onPlayItemClicked(trackNumber)
            }

            dataBinding.tvToolbarTitle.text = title
            playSelectedTrackFile(trackNumber?.minus(1)?:0)
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

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindAudioService()
    }

    private fun playTrackFile(filename: String, title: String?) {
        val audioService  = audioServiceBinder as AudioServiceBinder
        val filePath  = bookDetailsViewModel.getRemoteFilePath(filename)

        if(audioService.isStreamAudio()){
            audioService.stopAudio()
        }

//        if(audioProgressUpdateHandler == null){
//            createAudioProgressbarUpdater()
//        }

        audioService.setAudioFileUrl(filePath)
        audioService.setStreamAudio(true)
        audioService.setContext(this.activity!!.applicationContext)
        audioService.setAudioProgressUpdateHandler(audioProgressUpdateHandler!!)
        audioService.startAudio()

        Toast.makeText(this.context,"Playing #$title",Toast.LENGTH_SHORT).show()
    }

    private fun playSelectedTrackFile(currentPos:Int) {
        bookDetailsViewModel.audioBookTracks.value?.let {
            val audioService  = audioServiceBinder as AudioServiceBinder

            if(audioService.isStreamAudio()){
                audioService.stopAudio()
            }

            audioService.setMultipleTracks(it)
            audioService.setStreamAudio(true)
            audioService.setContext(this.activity!!.applicationContext)
            audioService.onCreate(bookId)
            audioService.setTrackPosition(currentPos)
            audioService.playTrack()

        }?:Toast.makeText(this.context,"Track is not available",Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        unBoundAudioService()
        super.onDestroy()
    }
}