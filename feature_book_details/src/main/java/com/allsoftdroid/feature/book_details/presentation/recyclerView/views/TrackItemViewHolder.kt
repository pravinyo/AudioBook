package com.allsoftdroid.feature.book_details.presentation.recyclerView.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.feature.book_details.databinding.BookMediaTrackItemBinding
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.presentation.recyclerView.adapter.TrackItemClickedListener

/**
 * ViewHolder Class for binding the data to the Layout and constructing layout for the display
 */
class TrackItemViewHolder private constructor(private val binding : BookMediaTrackItemBinding) : RecyclerView.ViewHolder(binding.root){

    // bind the data to the view
    fun bind(item: AudioBookTrackDomainModel,listener: TrackItemClickedListener){
        binding.track = item
        binding.clickListener = listener
        binding.executePendingBindings()
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