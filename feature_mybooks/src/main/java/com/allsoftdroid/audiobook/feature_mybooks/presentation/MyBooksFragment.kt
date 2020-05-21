package com.allsoftdroid.audiobook.feature_mybooks.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.audiobook.feature_mybooks.R
import com.allsoftdroid.audiobook.feature_mybooks.databinding.FragmentMybooksLayoutBinding
import com.allsoftdroid.audiobook.feature_mybooks.di.LocalBooksModule
import com.allsoftdroid.audiobook.feature_mybooks.presentation.recyclerView.ItemClickedListener
import com.allsoftdroid.audiobook.feature_mybooks.presentation.recyclerView.LocalBookAdapter
import com.allsoftdroid.audiobook.feature_mybooks.presentation.recyclerView.OptionsClickedListener
import com.allsoftdroid.audiobook.feature_mybooks.utils.Empty
import com.allsoftdroid.audiobook.feature_mybooks.utils.Started
import com.allsoftdroid.audiobook.feature_mybooks.utils.Success
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class MyBooksFragment : BaseUIFragment(),KoinComponent {

    private val localBooksViewModel : LocalBooksViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding:FragmentMybooksLayoutBinding = inflateLayout(inflater,
            R.layout.fragment_mybooks_layout,container,false)

        LocalBooksModule.injectFeature()

        dataBinding.lifecycleOwner = viewLifecycleOwner

        val adapter = LocalBookAdapter(
            this.requireActivity(),

            ItemClickedListener {bookId->
                //Navigate to display page
                val bundle = bundleOf("bookId" to bookId)

                this.findNavController()
                    .navigate(R.id.action_MyBooksFragment_to_AudioBookDetailsFragment,bundle)
            },

            OptionsClickedListener(
                onDeleteBook = {
                    localBooksViewModel.removeBook(it.bookIdentifier)
                },

                onRemoveChapters = {
                    localBooksViewModel.removeAllChapters(it.bookIdentifier)
                }
            )
        )

        dataBinding.recyclerViewBooks.adapter = adapter

        //recycler view layout manager
        dataBinding.recyclerViewBooks.apply {
            layoutManager = LinearLayoutManager(context)
        }

        dataBinding.toolbarBackArrow.setOnClickListener {
            onBackPressed()
        }

        localBooksViewModel.requestStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { status->
                when(status){
                    is Empty -> {
                        Timber.d("Empty result")
                        dataBinding.loadingProgressbar.visibility = View.GONE
                        dataBinding.noLocalBooks.visibility = View.VISIBLE
                        dataBinding.bookCount.visibility = View.GONE
                        dataBinding.recyclerViewBooks.visibility = View.GONE
                    }

                    is Started -> {
                        Timber.d("Started the request")
                        dataBinding.loadingProgressbar.visibility = View.VISIBLE
                        dataBinding.noLocalBooks.visibility = View.GONE
                        dataBinding.bookCount.visibility = View.GONE
                        dataBinding.recyclerViewBooks.visibility = View.GONE
                    }

                    is Success -> {
                        Timber.d("Received result:${status.list}")
                        dataBinding.loadingProgressbar.visibility = View.GONE
                        dataBinding.noLocalBooks.visibility = View.GONE

                        adapter.submitList(status.list)
                        dataBinding.recyclerViewBooks.visibility = View.VISIBLE

                        dataBinding.bookCount.apply {
                            visibility = View.VISIBLE
                            text = requireActivity().getString(R.string.books_in_storage,status.list.size)
                        }
                    }
                }
            }
        })

        return dataBinding.root
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        localBooksViewModel.loadFromCacheOrReload()
    }
}