package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.allsoftdroid.common.base.fragment.BaseContainerFragment

class MainPlayerFragment : BaseContainerFragment(){

    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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