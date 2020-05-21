package com.allsoftdroid.audiobook.feature_mybooks.presentation.recyclerView

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.audiobook.feature_mybooks.R
import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import timber.log.Timber

class LocalBookAdapter(
    private val context: Context,
    private val itemClickedListener: ItemClickedListener,
    private val optionsClickedListener: OptionsClickedListener
): ListAdapter<LocalBookDomainModel, RecyclerView.ViewHolder>(RandomBookDiffCallback()) {

    /**
     * Create view Holder of type BookViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LocalBookItemViewHolder.from(parent)
    }

    /**
     * Bind the ViewHolder with the data item
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is LocalBookItemViewHolder ->{
                val dataItem = getItem(position)
                holder.bind(dataItem,itemClickedListener)

                holder.buttonViewOptions.setOnClickListener {
                    showPopupMenu(dataItem,it)
                }
            }

            else -> throw Exception("View Holder type is unknown:$holder")
        }
    }

    private fun showPopupMenu(localBook: LocalBookDomainModel,view: View) {
        val popUp = PopupMenu(context,view)
        popUp.inflate(R.menu.local_books_option_menu)

        popUp.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.ItemOptions_removeAllChapters -> {
                    optionsClickedListener.onRemoveChaptersClicked(localBook)
                }

                R.id.ItemOptions_removeBook -> {

                    optionsClickedListener.onDeleteBookClicked(localBook)
                    Timber.d("remove clicked for ${localBook.bookTitle}")
                }
            }

            return@setOnMenuItemClickListener false
        }
        popUp.show()
    }
}


/**
class to smartly check for difference in new loaded list and old list
It enhance the performance of the recycler view
 */
class RandomBookDiffCallback : DiffUtil.ItemCallback<LocalBookDomainModel>(){
    /**
     * Compare items based on identifier fields
     */
    override fun areItemsTheSame(oldItem: LocalBookDomainModel, newItem: LocalBookDomainModel): Boolean {
        return oldItem.bookIdentifier==newItem.bookIdentifier
    }

    /**
     * Check every fields to verify for same content.
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: LocalBookDomainModel, newItem: LocalBookDomainModel): Boolean {
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
    fun onItemClicked(listenLater : LocalBookDomainModel) = clickListener(listenLater.bookIdentifier)
}

class OptionsClickedListener(
    val onDeleteBook : (identifier : LocalBookDomainModel)->Unit,
    val onRemoveChapters : (identifier : LocalBookDomainModel)->Unit)
{
    fun onDeleteBookClicked(listenLater : LocalBookDomainModel) = onDeleteBook(listenLater)
    fun onRemoveChaptersClicked(listenLater : LocalBookDomainModel) = onRemoveChapters(listenLater)
}
