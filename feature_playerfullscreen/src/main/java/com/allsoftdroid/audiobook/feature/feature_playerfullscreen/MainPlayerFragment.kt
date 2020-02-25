package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.databinding.LayoutMainFragmentBinding
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import org.koin.android.ext.android.inject

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback
    private val mainPlayerViewModel: MainPlayerViewModel by inject()
    private val bookId = "tom_sawyer_librivox"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        FeatureMainPlayerModule.injectFeature()
        val binding:LayoutMainFragmentBinding = inflateLayout(inflater,R.layout.layout_main_fragment,container)

        binding.viewModel = mainPlayerViewModel

        binding.toolbarBackButton.setOnClickListener {
            handleBackPressEvent()
        }

        mainPlayerViewModel.setBookIdentifier(bookId)

        return binding.root
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