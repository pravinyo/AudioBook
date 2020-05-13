package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.recyclerView

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel

class ListenLaterAdapter(private val listener: ItemClickedListener): ListAdapter<ListenLaterItemDomainModel, RecyclerView.ViewHolder>(RandomBookDiffCallback()) {

    /**
     * Create view Holder of type BookViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListenLaterItemViewHolder.from(parent)
    }

    /**
     * Bind the ViewHolder with the data item
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ListenLaterItemViewHolder ->{
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
class RandomBookDiffCallback : DiffUtil.ItemCallback<ListenLaterItemDomainModel>(){
    /**
     * Compare items based on identifier fields
     */
    override fun areItemsTheSame(oldItem: ListenLaterItemDomainModel, newItem: ListenLaterItemDomainModel): Boolean {
        return oldItem.identifier==newItem.identifier
    }

    /**
     * Check every fields to verify for same content.
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ListenLaterItemDomainModel, newItem: ListenLaterItemDomainModel): Boolean {
        /*
        Since book is data class so here all the fields are automatically checked
         */
        return oldItem == newItem
    }

}

/*
listener to check for the click event
 */
class ItemClickedListener(val clickListener : (identifier : String)->Unit){
    fun onItemClicked(listenLater : ListenLaterItemDomainModel) = clickListener(listenLater.identifier)
}