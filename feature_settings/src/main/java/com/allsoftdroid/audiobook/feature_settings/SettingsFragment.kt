package com.allsoftdroid.audiobook.feature_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.allsoftdroid.audiobook.feature_settings.databinding.LayoutSettingsFragmentBinding
import com.allsoftdroid.common.base.fragment.BaseUIFragment

class SettingsFragment : BaseUIFragment() {

    companion object{
        private const val SETTINGS_FRAGMENT = "Settings_fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding:LayoutSettingsFragmentBinding = inflateLayout(inflater,
            R.layout.layout_settings_fragment,container,false)

        dataBinding.lifecycleOwner = viewLifecycleOwner

        dataBinding.toolbarBackButton.setOnClickListener {
            onBackPressed()
        }

        loadSettingsUI()

        return dataBinding.root
    }

    private fun loadSettingsUI() {
        val fragment = childFragmentManager.findFragmentByTag(SETTINGS_FRAGMENT)

        if(fragment == null){
            childFragmentManager.beginTransaction()
                .add(R.id.fragment_container,SettingsPreferenceFragment(), SETTINGS_FRAGMENT)
                .commit()
        }else{
            childFragmentManager.beginTransaction()
                .show(fragment)
                .commit()
        }
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressed()
    }
}