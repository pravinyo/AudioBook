package com.allsoftdroid.feature.book_details.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAudiobookDetailsBinding


class AudioBookDetailsFragment : BaseContainerFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val databinding : FragmentAudiobookDetailsBinding = inflateLayout(inflater,R.layout.fragment_audiobook_details,container)

        val argument = arguments?.getString("bookId")?:""

        Toast.makeText(context,argument,Toast.LENGTH_SHORT).show()

        return databinding.root
    }
}