package com.allsoftdroid.feature.book_details.presentation

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.allsoftdroid.common.base.fragment.BaseContainerFragment
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.databinding.FragmentAdditionalDetailsBackdropBinding
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel

class BackDropFragment : BaseContainerFragment() {

    /**
    Lazily initialize the view model
     */
    private lateinit var bookDetailsViewModel: BookDetailsViewModel
    private lateinit var binding: FragmentAdditionalDetailsBackdropBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val dataBinding :FragmentAdditionalDetailsBackdropBinding = inflateLayout(inflater,
            R.layout.fragment_additional_details_backdrop,container)

        dataBinding.lifecycleOwner = viewLifecycleOwner
        binding = dataBinding

        return dataBinding.root
    }

    fun setSharedViewModel(sharedVM : BookDetailsViewModel){
        this.bookDetailsViewModel = sharedVM

        binding.tvBookDescText.apply {

            bookDetailsViewModel.audioBookMetadata.observe(viewLifecycleOwner, Observer {
                if(text.isEmpty()){
                    text = convertHtmlToText(it.description)
                }
            })

            bookDetailsViewModel.additionalBookDetails.observe(viewLifecycleOwner, Observer {
                text = convertHtmlToText(it.description)
            })
        }


        bookDetailsViewModel.audioBookMetadata.observe(viewLifecycleOwner, Observer {
            binding.tvBookPublisher.text = it.creator
        })
    }

    @Suppress("DEPRECATION")
    fun convertHtmlToText(text:String?) = text?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(it)
        }
    } .toString()
}