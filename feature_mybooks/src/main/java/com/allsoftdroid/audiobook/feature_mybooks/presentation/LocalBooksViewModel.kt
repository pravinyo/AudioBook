package com.allsoftdroid.audiobook.feature_mybooks.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_mybooks.di.LocalBooksModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_mybooks.di.LocalBooksModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature_mybooks.utils.Empty
import com.allsoftdroid.audiobook.feature_mybooks.utils.RequestStatus
import com.allsoftdroid.common.base.extension.Event
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

class LocalBooksViewModel : ViewModel(),KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var _requestStatus = MutableLiveData<Event<RequestStatus>>()
    val requestStatus : LiveData<Event<RequestStatus>> = _requestStatus

    init {
        _requestStatus.value = Event(Empty)
    }


    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}