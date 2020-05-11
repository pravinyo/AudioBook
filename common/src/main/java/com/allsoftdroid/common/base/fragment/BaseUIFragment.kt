package com.allsoftdroid.common.base.fragment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback

abstract class BaseUIFragment : BaseContainerFragment() {

    private lateinit var callback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            handleBackPressEvent(callback)
        }

        callback.isEnabled = true
    }

    abstract fun handleBackPressEvent(callback: OnBackPressedCallback)

    fun onBackPressed(){
        handleBackPressEvent(callback)
    }
}