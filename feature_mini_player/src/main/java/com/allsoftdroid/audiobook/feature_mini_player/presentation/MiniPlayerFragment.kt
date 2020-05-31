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
import it.sephiroth.android.library.xtooltip.Tooltip
import org.koin.android.ext.android.inject
import timber.log.Timber

class MiniPlayerFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private val miniPlayerViewModel: MiniPlayerViewModel by inject()
    var tooltip:Tooltip? = null
    private lateinit var refBinding:FragmentMiniPlayerBinding

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

        refBinding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val metrics = resources.displayMetrics
        val gravity = Tooltip.Gravity.TOP

        tooltip = Tooltip.Builder(view.context)
            .anchor(refBinding.ivMiniPlayerAlbumArt,70,70,false)
            .text(getString(R.string.tooltip_open_player_message))
            .maxWidth(metrics.widthPixels / 2)
            .arrow(true)
            .floatingAnimation(Tooltip.Animation.DEFAULT)
            .showDuration(3000)
            .overlay(true)
            .create()

        tooltip
            ?.doOnHidden {
                tooltip = null
            }
            ?.show(refBinding.root, gravity, true)
    }
}