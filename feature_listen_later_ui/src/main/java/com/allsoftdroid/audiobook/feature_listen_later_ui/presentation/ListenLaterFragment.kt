package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.audiobook.feature_listen_later_ui.R
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.databinding.FragmentListenLaterLayoutBinding
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Empty
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Started
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Success
import com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.recyclerView.ItemClickedListener
import com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.recyclerView.ListenLaterAdapter
import com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.recyclerView.OptionsClickedListener
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent

class ListenLaterFragment : BaseUIFragment(),KoinComponent {

    private val listenLaterViewModel: ListenLaterViewModel by inject()
    private lateinit var bindingRef:FragmentListenLaterLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FeatureListenLaterModule.injectFeature()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dataBinding:FragmentListenLaterLayoutBinding = inflateLayout(inflater,
            R.layout.fragment_listen_later_layout,container,false)

        dataBinding.lifecycleOwner = viewLifecycleOwner
        dataBinding.viewModel = listenLaterViewModel

        //val audio book adapter
        val listenLaterAdapter = ListenLaterAdapter(
            requireContext(),

            ItemClickedListener {
                Toast.makeText(this.requireActivity(),"Book Id:$it",Toast.LENGTH_SHORT).show()
            },

            OptionsClickedListener(
                onRemove = {
                    listenLaterViewModel.removeItem(it.identifier)
                },

                onShare = {

                }
            )
        )

        //attach adapter to recycler view
        dataBinding.recyclerViewBooks.adapter = listenLaterAdapter

        //recycler view layout manager
        dataBinding.recyclerViewBooks.apply {
            layoutManager = LinearLayoutManager(context)
        }

        dataBinding.toolbarBackArrow.setOnClickListener {
            onBackPressed()
        }

        listenLaterViewModel.requestStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { request ->
                when(request){
                    is Started -> {
                        loading()
                    }

                    is Empty -> {
                        noDataUI()
                    }

                    is Success -> {
                        if (request.list.isEmpty()){
                            noDataUI()
                        }else
                        {
                            dataAvailable(request.list)
                            listenLaterAdapter.submitList(request.list)
                        }
                    }
                }
            }
        })

        bindingRef = dataBinding

        return dataBinding.root
    }

    private fun noDataUI() {
        removeLoading()
    }

    private fun dataAvailable(list: List<ListenLaterItemDomainModel>) {

        bindingRef.bookStatsCount.apply {
            this.text = "${list.size} Books"
        }

        bindingRef.loadingProgressbar.apply {
            this.visibility = View.GONE
        }

        bindingRef.recyclerViewBooks.apply {
            this.visibility = View.VISIBLE
        }

        bindingRef.bookStats.apply {
            this.visibility = View.VISIBLE
        }
    }

    private fun removeLoading(){
        bindingRef.loadingProgressbar.apply {
            this.visibility = View.GONE
        }

        bindingRef.noData.apply {
            this.visibility = View.VISIBLE
        }
    }

    private fun loading(){
        bindingRef.loadingProgressbar.apply {
            this.visibility = View.VISIBLE
        }

        bindingRef.noData.apply {
            this.visibility = View.GONE
        }

        bindingRef.recyclerViewBooks.apply {
            this.visibility = View.GONE
        }

        bindingRef.bookStats.apply {
            this.visibility = View.GONE
        }
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressed()
    }
}