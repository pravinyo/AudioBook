package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.VIEW_MODEL_SCOPE
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

class ListenLaterViewModel : ViewModel(), KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))


    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}