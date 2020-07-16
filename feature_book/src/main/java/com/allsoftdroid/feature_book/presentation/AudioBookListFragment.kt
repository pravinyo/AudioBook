package com.allsoftdroid.feature_book.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.fragment.BaseUIFragment
import com.allsoftdroid.common.base.network.StoreUtils
import com.allsoftdroid.common.base.store.userAction.OpenDownloadUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.data.network.Utils
import com.allsoftdroid.feature_book.databinding.FragmentAudiobookListBinding
import com.allsoftdroid.feature_book.di.FeatureBookModule
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookAdapter
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.AudioBookItemClickedListener
import com.allsoftdroid.feature_book.presentation.recyclerView.adapter.PaginationListener
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import com.allsoftdroid.feature_book.utils.NetworkState
import com.google.android.material.navigation.NavigationView
import org.koin.android.ext.android.inject
import timber.log.Timber


class AudioBookListFragment : BaseUIFragment(){

    /**
    Lazily initialize the view model
     */
    private val booksViewModel: AudioBookListViewModel by inject()
    @VisibleForTesting var bundleShared: Bundle = Bundle.EMPTY

    private lateinit var drawer: DrawerLayout
    private lateinit var navView : NavigationView

    private val userActionEventStore: UserActionEventStore by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding:FragmentAudiobookListBinding = inflateLayout(inflater,R.layout.fragment_audiobook_list,container)

        FeatureBookModule.injectFeature()

        binding.lifecycleOwner = viewLifecycleOwner
        binding.audioBookListViewModel = booksViewModel
        drawer = binding.drawerLayout
        navView = binding.navView

        setupUI(binding)

        binding.swipeBookRefresh.setOnRefreshListener {
            booksViewModel.refresh()
            binding.swipeBookRefresh.isRefreshing = false
        }

        ViewCompat.setTranslationZ(binding.root, 0f)
        return binding.root
    }

    private fun setupDrawer() {
        navView.setNavigationItemSelectedListener {

            drawer.closeDrawer(GravityCompat.START)

            when(it.itemId){
                R.id.nav_item_downloads -> {
                    navigateToDownloadsActivity()
                    return@setNavigationItemSelectedListener true
                }

                R.id.nav_item_settings -> {
                    this.findNavController()
                        .navigate(R.id.action_AudioBookListFragment_to_SettingsFragment)
                }

                R.id.nav_item_listen_later -> {
                    this.findNavController()
                        .navigate(R.id.action_AudioBookListFragment_to_ListenLaterFragment)
                }

                R.id.nav_item_my_book -> {
                    this.findNavController()
                        .navigate(R.id.action_AudioBookListFragment_to_MyBooksFragment)
                }

                R.id.nav_item_rate -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(StoreUtils.getStoreUrl(requireActivity()))
                        setPackage(getString(R.string.android_vending_package))
                    }
                    startActivity(intent)
                }
            }

            return@setNavigationItemSelectedListener false
        }

        val headerView = navView.getHeaderView(0)

        val profilePic = headerView.findViewById<ImageView>(R.id.nav_header_imageView)
        profilePic.setOnClickListener {
            Toast.makeText(it.context,getString(R.string.pic_attribution),Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar(binding:FragmentAudiobookListBinding){
        binding.toolbarBookSearch.setOnClickListener{
            binding.etToolbarSearch.text.clear()
            booksViewModel.onSearchItemPressed()
        }

        binding.etToolbarSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {booksViewModel.setSearchOrClose(isSearchBtn = it.isNotEmpty() && it.length>3)}
            }
        })

        binding.etToolbarSearch.setOnKeyListener { view, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN){
                binding.ivSearch.performClick()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.ivSearchCancel.setOnClickListener {
            binding.etToolbarSearch.clearFocus()
            hideKeyboard()

            booksViewModel.onSearchFinished()
        }

        binding.toolbarNavHamburger.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    private fun setupUI(binding:FragmentAudiobookListBinding){
        setupToolbar(binding)
        setupDrawer()

        //val audio book adapter
        val bookAdapter = AudioBookAdapter(AudioBookItemClickedListener {
            booksViewModel.onBookItemClicked(it)
        })

        //attach adapter to recycler view
        binding.recyclerViewBooks.adapter = bookAdapter

        //recycler view layout manager
        binding.recyclerViewBooks.apply {
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object : PaginationListener(
                layoutManager = layoutManager as LinearLayoutManager,
                pageSize = Utils.Books.DEFAULT_ROW_COUNT){

                override fun loadNext() {
                    booksViewModel.loadNextData()
                }

                override fun isLoading(): Boolean {
                    return booksViewModel.networkResponse.value?.peekContent() == NetworkState.LOADING
                }
            })
        }

        //Observe the books list and update the list as soon as we get the update
        booksViewModel.audioBooks.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(!booksViewModel.isSearching){
                    if(it.isNotEmpty()) setVisibility(binding.networkNoConnection,set=false)
                    bookAdapter.submitList(it)
                }
            }
        })

        booksViewModel.itemClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { bookId ->
                //Navigate to display page
                val bundle = bundleOf("bookId" to bookId)
                bundleShared = bundle

                this.findNavController()
                    .navigate(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle)
            }
        })

        booksViewModel.networkResponse.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { networkState ->
                when(networkState){
                    NetworkState.LOADING -> {
                        setVisibility(binding.loadingProgressbar,set = true)
                        setVisibility(binding.networkNoConnection,set=false)
                    }

                    NetworkState.COMPLETED -> {
                        setVisibility(binding.loadingProgressbar,set=false)
                        setVisibility(binding.networkNoConnection,set=false)
                    }

                    NetworkState.ERROR -> {
                        setVisibility(binding.loadingProgressbar,set=false)

                        if(booksViewModel.audioBooks.value.isNullOrEmpty()){
                            setVisibility(binding.networkNoConnection,set=true)
                        }else if(booksViewModel.searchBooks.value.isNullOrEmpty()){
                            setVisibility(binding.networkNoConnection,set=true)
                        }

                        Toast.makeText(context,getString(R.string.network_error_message),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        booksViewModel.searchBooks.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                val prev = bookAdapter.itemCount
                bookAdapter.submitList(it)
                if (prev >0) binding.recyclerViewBooks.scrollToPosition(prev-1)
                Timber.d("Adapter size: $prev and List size:${it.size} and scroll to : ${prev-1}")
                setVisibility(binding.networkNoConnection,set=false)
            }else{
                setVisibility(binding.networkNoConnection,set=true)
            }
        })

        binding.ivSearch.setOnClickListener {
            val searchText = binding.etToolbarSearch.text.trim().toString()
            binding.etToolbarSearch.clearFocus()
            hideKeyboard()

            if(searchText.length>3){
                bookAdapter.submitList(null)
                booksViewModel.search(query = searchText)
            }
        }
    }

    private fun navigateToDownloadsActivity() {
        userActionEventStore.publish(
            Event(OpenDownloadUI(this::class.java.simpleName))
        )
    }

    private fun setVisibility(view: View, set: Boolean) {
        view.visibility = if(set) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val view = this.view?.rootView
        view?.let { v ->
            val imm = this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun handleBackPressEvent(callback: OnBackPressedCallback) {
        Timber.d("backPress:Back pressed")
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> {
                drawer.closeDrawer(drawer)
            }

            booksViewModel.isSearching -> {
                booksViewModel.apply {
                    cancelSearchRequest()
                    onSearchFinished()
                    loadRecentBookList()
                }
            }

            else -> {
                callback.isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }
}