package com.allsoftdroid.audiobook.feature_mini_player.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.allsoftdroid.audiobook.feature_mini_player.R
import com.allsoftdroid.audiobook.feature_mini_player.databinding.FragmentMiniPlayerBinding
import com.allsoftdroid.audiobook.feature_mini_player.di.FeatureMiniPlayerModule
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModel
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class MiniPlayerFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private val miniPlayerViewModel: MiniPlayerViewModel by inject()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("Mini Player fragment created")
        val binding : FragmentMiniPlayerBinding = inflateLayout(inflater,R.layout.fragment_mini_player,container)

        FeatureMiniPlayerModule.injectFeature()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = miniPlayerViewModel

        miniPlayerViewModel.shouldWaitForPlayer.observe(viewLifecycleOwner, Observer { waitForPlayer ->
            if(waitForPlayer){
                Toast.makeText(this.activity,"Please wait, Preparing",Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }
}