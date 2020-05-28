package com.allsoftdroid.audiobook.feature_mybooks.presentation.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import com.allsoftdroid.audiobook.feature_mybooks.databinding.MyBooksItemLayoutBinding

class LocalBookItemViewHolder private constructor(private val binding : MyBooksItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var buttonViewOptions: View

    // bind the data to the view
    fun bind(item: LocalBookDomainModel, itemClickedListener: ItemClickedListener) {
        binding.book = item
        binding.clickListener = itemClickedListener
        buttonViewOptions = binding.ItemOptions
        binding.executePendingBindings()
    }

    //construct the viewholder
    companion object {
        fun from(parent: ViewGroup): LocalBookItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MyBooksItemLayoutBinding.inflate(layoutInflater, parent, false)

            return LocalBookItemViewHolder(binding)
        }
    }
}