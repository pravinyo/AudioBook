package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.*
import com.allsoftdroid.common.base.extension.Event
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

class ListenLaterViewModel(private val repository:IListenLaterRepository) : ViewModel(), KoinComponent {
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

    fun loadDefault() {
        viewModelScope.launch {
            _requestStatus.value = Event(Started)

            val data =
                withContext(coroutineContext) { repository.getBooksInLIFO() }

            if(!data.isNullOrEmpty()){
                _requestStatus.value = Event(Success(data))
            }else{
                _requestStatus.value = Event(Empty)
            }
        }
    }

    fun removeItem(identifier:String){
        viewModelScope.launch {
            withContext(coroutineContext){
                repository.removeBookById(identifier)
            }

            loadDefault()
        }
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}