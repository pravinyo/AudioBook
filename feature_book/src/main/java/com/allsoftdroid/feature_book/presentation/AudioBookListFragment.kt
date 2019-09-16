package com.allsoftdroid.feature_book.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import kotlinx.android.synthetic.main.fragment_audiobook_list.*

class AudioBookListFragment : Fragment(){

    /**
    Lazily initialize the view model
     */
    private val viewModel: AudioBookViewModel by lazy {

        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onCreated()"
        }

        ViewModelProviders.of(this, AudioBookViewModelFactory(
                GetAudioBookListUsecase(AudioBookRepositoryImpl()),
                activity.application)
        )
            .get(AudioBookViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_audiobook_list, null).also {
            Log.i(AudioBookListFragment::class.java.simpleName,"Loading list fragment")
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.audioBooks.observe(viewLifecycleOwner, Observer {
          it?.let {
              it.map {book ->
                  text.append("${book.title}\n")
              }

              Log.i(AudioBookListFragment::class.java.simpleName,"Content:$text")
          }
        })
    }

}