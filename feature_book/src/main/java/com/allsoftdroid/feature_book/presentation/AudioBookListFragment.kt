package com.allsoftdroid.feature_book.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.feature_book.NetworkState
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.databinding.FragmentAudiobookListBinding
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookAdapter
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookItemClickedListener
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModelFactory

class AudioBookListFragment : BaseContainerFragment(){

    /**
    Lazily initialize the view model
     */
    private val booksViewModel: AudioBookListViewModel by lazy {

        val activity = requireNotNull(this.activity) {
            "You can only access the booksViewModel after onCreated()"
        }

        ViewModelProviders.of(this, AudioBookListViewModelFactory(activity.application))
            .get(AudioBookListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding:FragmentAudiobookListBinding = inflateLayout(inflater,R.layout.fragment_audiobook_list,container)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.audioBookListViewModel = booksViewModel

        //val audio book adapter
        val bookAdapter = AudioBookAdapter(AudioBookItemClickedListener {
            booksViewModel.onBookItemClicked(it)
        })

        //attach adapter to recycler view
        binding.recyclerViewBooks.adapter = bookAdapter

        //recycler view layout manager
        binding.recyclerViewBooks.apply {
            layoutManager = LinearLayoutManager(context)
        }

        //Observe the books list and update the list as soon as we get the update
        booksViewModel.audioBooks.observe(viewLifecycleOwner, Observer {
            it?.let {
                bookAdapter.submitList(it)
            }
        })

        booksViewModel.itemClicked.observe(this, Observer {
            it.getContentIfNotHandled()?.let { bookId ->
                //Navigate to display page
                val bundle = bundleOf("bookId" to bookId)

                this.findNavController()
                    .navigate(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle)
            }
        })

        booksViewModel.networkResponse.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){
                    NetworkState.LOADING -> {
                        binding.loadingProgressbar.visibility = View.VISIBLE
                    }

                    NetworkState.COMPLETED -> {
                        binding.loadingProgressbar.visibility = View.GONE
                    }

                    NetworkState.ERROR -> {
                        binding.loadingProgressbar.visibility = View.GONE
                        Toast.makeText(context,"Network Error",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        return binding.root
    }
}