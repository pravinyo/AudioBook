package com.allsoftdroid.feature_book.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.databinding.FragmentAudiobookListBinding
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookAdapter
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModelFactory

class AudioBookListFragment : Fragment(){

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
        val binding:FragmentAudiobookListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_audiobook_list,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.audioBookListViewModel = booksViewModel

        //val audio book adapter
        val bookAdapter = AudioBookAdapter()

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


        return binding.root
    }
}