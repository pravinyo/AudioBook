package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import org.koin.android.ext.android.inject

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback
    private val booksViewModel: MainPlayerViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        FeatureMainPlayerModule.injectFeature()

        val view = inflater.inflate(R.layout.layout_main_fragment,container,false)
        view.findViewById<ImageView>(R.id.toolbar_back_button).setOnClickListener {
            handleBackPressEvent()
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            handleBackPressEvent()
        }

        callback.isEnabled = true
    }

    private fun handleBackPressEvent(){
        this.findNavController().navigateUp()
    }
}