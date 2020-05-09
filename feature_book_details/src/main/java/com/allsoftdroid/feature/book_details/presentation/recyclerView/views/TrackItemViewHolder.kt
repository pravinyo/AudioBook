package com.allsoftdroid.feature.book_details.presentation.recyclerView.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.common.base.store.downloader.*
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.BookMediaTrackItemBinding
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.DownloadItemClickedListener
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener
import com.allsoftdroid.feature.book_details.utils.*
import timber.log.Timber

/**
 * ViewHolder Class for binding the data to the Layout and constructing layout for the display
 */
class TrackItemViewHolder private constructor(private val binding : BookMediaTrackItemBinding) : RecyclerView.ViewHolder(binding.root){

    // bind the data to the view
    @SuppressLint("CheckResult")
    fun bind(downloadEventStore: DownloadEventStore, bookId:String, item: AudioBookTrackDomainModel, clickedListener: TrackItemClickedListener, downloadItemClickedListener: DownloadItemClickedListener){
        binding.track = item
        binding.clickListener = clickedListener
        binding.downloadListener = downloadItemClickedListener

        downloadEventStore.observe().subscribe { event->
            event.peekContent().let {
                if(it.bookId == bookId && it.chapterIndex-1==adapterPosition){
                    when(it){
                        is Progress ->{

                            showProgress()
                            binding.downloadProgress.progress = it.percent.toInt()
                            Timber.d("download event for chapterIndex:${it.chapterIndex} and position is :$adapterPosition with progress:${it.percent.toInt()}")
                        }

                        is Downloaded ->{
                            setItemIcon(DOWNLOADED)
                        }

                        is Download -> {

                        }

                        is Downloading ->{
                            setItemIcon(DOWNLOADING)
                        }

                        is DownloadNothing ->{
                            setItemIcon(NOTHING)
                        }
                    }

                    Timber.d("download event for chapterIndex:${it.chapterIndex} and position is :$adapterPosition")
                }
            }
        }

        binding.executePendingBindings()
    }

    private fun showProgress() {
        binding.downloadProgress.visibility = View.VISIBLE
        binding.downloadIcon.visibility =View.GONE
    }

    private fun hideProgress() {
        binding.downloadProgress.visibility = View.GONE
        binding.downloadIcon.visibility =View.VISIBLE
    }

    private fun setItemIcon(downloadStatus:DownloadStatusEvent){
        binding.downloadIcon.apply {
            setImageResource(
                when(downloadStatus){
                    is DOWNLOADING,is PROGRESS -> R.drawable.close_circle_outline
                    is DOWNLOADED -> R.drawable.download_check
                    is NOTHING,is CANCELLED -> R.drawable.download_outline
                }
            )
        }

        hideProgress()
    }

    //construct the viewholder
    companion object{
        fun from(parent: ViewGroup) : TrackItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = BookMediaTrackItemBinding.inflate(layoutInflater,parent,false)

            return TrackItemViewHolder(binding)
        }
    }
}