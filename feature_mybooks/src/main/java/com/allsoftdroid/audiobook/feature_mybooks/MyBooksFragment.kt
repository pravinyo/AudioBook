package com.allsoftdroid.audiobook.feature_mybooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.allsoftdroid.audiobook.feature_mybooks.databinding.FragmentMybooksLayoutBinding
import com.allsoftdroid.common.base.fragment.BaseUIFragment

class MyBooksFragment : BaseUIFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding:FragmentMybooksLayoutBinding = inflateLayout(inflater,
            R.layout.fragment_mybooks_layout,container,false)

        return dataBinding.root
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressed()
    }
}