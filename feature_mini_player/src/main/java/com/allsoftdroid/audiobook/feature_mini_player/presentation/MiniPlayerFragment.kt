package com.allsoftdroid.audiobook.feature_mini_player.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.allsoftdroid.audiobook.feature_mini_player.R
import com.allsoftdroid.audiobook.feature_mini_player.databinding.FragmentMiniPlayerBinding
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModel
import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModelFactory
import com.allsoftdroid.common.base.fragment.BaseContainerFragment

class MiniPlayerFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private val miniPlayerViewModel: MiniPlayerViewModel by lazy {

        val activity = requireNotNull(this.activity) {
            "You can only access the booksViewModel after onCreated()"
        }

        ViewModelProviders.of(this, MiniPlayerViewModelFactory(activity.application))
            .get(MiniPlayerViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding : FragmentMiniPlayerBinding = inflateLayout(inflater,R.layout.fragment_mini_player,container)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = miniPlayerViewModel

        return binding.root
    }
}