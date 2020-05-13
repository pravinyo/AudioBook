package com.allsoftdroid.audiobook.feature_listen_later_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.allsoftdroid.audiobook.feature_listen_later_ui.databinding.FragmentListenLaterLayoutBinding
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import org.koin.android.ext.android.inject

class ListenLaterFragment : BaseUIFragment() {

    val listenLaterViewModel:ListenLaterViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding:FragmentListenLaterLayoutBinding = inflateLayout(inflater,R.layout.fragment_listen_later_layout,container,false)
        FeatureListenLaterModule.unLoadModules()

        return dataBinding.root
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        FeatureListenLaterModule.unLoadModules()
    }
}