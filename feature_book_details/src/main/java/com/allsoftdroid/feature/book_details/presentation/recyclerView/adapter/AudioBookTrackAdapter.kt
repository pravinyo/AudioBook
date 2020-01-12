package com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.presentation.recyclerView.views.TrackItemViewHolder

/**
 * Recycler Adapter uses efficient way to find the change in the list item.
 * It uses DiffUtils for efficient management of the recycler view list item
 */

class AudioBookTrackAdapter(private val listener: TrackItemClickedListener): ListAdapter<AudioBookTrackDomainModel, RecyclerView.ViewHolder>(TrackDiffCallback()) {

    /**
     * Create view Holder of type BookViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TrackItemViewHolder.from(parent)
    }

    /**
     * Bind the ViewHolder with the data item
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is TrackItemViewHolder ->{
                val dataItem = getItem(position)
                holder.bind(dataItem,listener)
            }

            else -> throw Exception("View Holder type is unknown:$holder")
        }
    }
}




/**
class to smartly check for difference in new loaded list and old list
It enhance the performance of the recycler view
 */
class TrackDiffCallback : DiffUtil.ItemCallback<AudioBookTrackDomainModel>(){
    /**
     * Compare items based on name field as it is unique among track
     */
    override fun areItemsTheSame(oldItem: AudioBookTrackDomainModel, newItem: AudioBookTrackDomainModel): Boolean {
        return oldItem.filename==newItem.filename && oldItem.isPlaying != newItem.isPlaying
    }

    /**
     * Check every fields to verify for same content.
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AudioBookTrackDomainModel, newItem: AudioBookTrackDomainModel): Boolean {
        /*
        Since book is data class so here all the fields are automatically checked
         */
        return oldItem == newItem
    }
}


/*
listener to check for the click event
 */
class TrackItemClickedListener(val clickListener : (trackNumber : Int?,filename:String,trackTitle:String?)->Unit){
    fun onTrackItemClicked(track : AudioBookTrackDomainModel) = clickListener(track.trackNumber,track.filename,track.title)
}