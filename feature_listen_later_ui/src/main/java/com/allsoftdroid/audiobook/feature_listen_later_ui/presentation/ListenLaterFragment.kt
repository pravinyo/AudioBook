package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import com.allsoftdroid.common.base.utils.ShareUtils
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.SortType
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import com.allsoftdroid.common.base.network.StoreUtils
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

            ItemClickedListener {bookId->
                //Navigate to display page
                val bundle = bundleOf("bookId" to bookId)

                this.findNavController()
                    .navigate(R.id.action_ListenLaterFragment_to_AudioBookDetailsFragment,bundle)
            },

            OptionsClickedListener(
                onRemove = {
                    listenLaterViewModel.removeItem(it.identifier)
                },

                onShare = {item->
                    this.activity?.let {parent->
                        ShareUtils.share(
                            context = parent,
                            subject = "${item.title} on AudioBook",
                            txt = "Listen ${item.title} written by '${item.author}' on AudioBook App," +
                                    " Start Listening: ${StoreUtils.getStoreUrl(requireActivity())}"
                        )
                    }
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

        dataBinding.sortList.setOnClickListener {
            showPopMenu(it)
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

    private fun showPopMenu(view: View?) {
        view?.let {
            val popUp = PopupMenu(requireContext(),it)
            popUp.inflate(R.menu.sort_options_menu)

            popUp.setOnMenuItemClickListener {item ->

                listenLaterViewModel.setCurrentShortType(when(item.itemId){
                    R.id.menu_options_sort_latest_first -> {
                        SortType.LatestFirst
                    }

                    R.id.menu_options_sort_oldest_first -> {
                        SortType.OldestFirst
                    }

                    R.id.menu_options_sort_shortest_first->{
                        SortType.ShortestFirst
                    }

                    else -> SortType.LatestFirst
                })

                return@setOnMenuItemClickListener false
            }
            popUp.show()

        }
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

    override fun onResume() {
        super.onResume()
        listenLaterViewModel.loadList()
    }
}