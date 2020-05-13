package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.databinding.LayoutListenLaterItemBinding

class ListenLaterItemViewHolder private constructor(private val binding : LayoutListenLaterItemBinding) : RecyclerView.ViewHolder(binding.root) {

    // bind the data to the view
    fun bind(item: ListenLaterItemDomainModel, listener: ItemClickedListener) {
        binding.book = item
        binding.clickListener = listener
        binding.executePendingBindings()
    }

    //construct the viewholder
    companion object {
        fun from(parent: ViewGroup): ListenLaterItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = LayoutListenLaterItemBinding.inflate(layoutInflater, parent, false)

            return ListenLaterItemViewHolder(binding)
        }
    }
}