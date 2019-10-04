package com.allsoftdroid.common.base.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseContainerFragment : Fragment(){

    fun<T :ViewDataBinding> inflateLayout(inflater:LayoutInflater,layout:Int,parentView:ViewGroup?,attachToParent:Boolean = false):T = DataBindingUtil.inflate(
        inflater,
        layout,
        parentView,
        attachToParent
    )
}