package com.allsoftdroid.audiobook.feature.feature_playerfullscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di.FeatureMainPlayerModule.VIEW_MODEL_SCOPE
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

class MainPlayerViewModel : ViewModel(), KoinComponent {

    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var _playingTrackDetails = MutableLiveData<PlayingTrackDetails>()
    val playingTrackDetails:LiveData<PlayingTrackDetails> = _playingTrackDetails

    fun setBookIdentifier(identifier:String){
        _playingTrackDetails.value = PlayingTrackDetails(
            bookIdentifier = identifier,
            bookTitle = "Book Title goes here",
            name = "Track Name goes here",
            chapterIndex = 1,
            totalChapter = 30
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}