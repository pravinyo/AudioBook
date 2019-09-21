package com.allsoftdroid.feature_book.presentation.recyclerView.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.presentation.recyclerView.views.AudioBookItemViewHolder

/**
 * Recycler Adapter uses efficient way to find the change in the list item.
 * It uses DiffUtils for efficient management of the recycler view list item
 */

class AudioBookAdapter: ListAdapter<AudioBookDomainModel, RecyclerView.ViewHolder>(RandomBookDiffCallback()) {

    /**
     * Create view Holder of type BookViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AudioBookItemViewHolder.from(parent)
    }

    /**
     * Bind the ViewHolder with the data item
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is AudioBookItemViewHolder ->{
                val dataItem = getItem(position)
                holder.bind(dataItem)
            }

            else -> throw Exception("View Holder type is unknown:$holder")
        }
    }
}




/**
class to smartly check for difference in new loaded list and old list
It enhance the performance of the recycler view
 */
class RandomBookDiffCallback : DiffUtil.ItemCallback<AudioBookDomainModel>(){
    /**
     * Compare items based on identifier fields
     */
    override fun areItemsTheSame(oldItem: AudioBookDomainModel, newItem: AudioBookDomainModel): Boolean {
        return oldItem.mId==newItem.mId
    }

    /**
     * Check every fields to verify for same content.
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AudioBookDomainModel, newItem: AudioBookDomainModel): Boolean {
        /*
        Since book is data class so here all the fields are automatically checked
         */
        return oldItem == newItem
    }

}