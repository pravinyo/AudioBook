package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Empty
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.RequestStatus
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Started
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.Success
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IListenLaterRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase.ExportUserData
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase.ImportUserData
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.SortType
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.StringUtility
import com.allsoftdroid.common.base.extension.Event
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class ListenLaterViewModel(
    private val repository: IListenLaterRepository,
    private val exportUserData: ExportUserData,
    private val importUserData: ImportUserData
) : ViewModel(), KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private var currentShortType = SortType.LatestFirst

    private var _requestStatus = MutableLiveData<Event<RequestStatus>>()
    val requestStatus : LiveData<Event<RequestStatus>> = _requestStatus

    private var _notification = MutableLiveData<Event<String>>()
    val notification : LiveData<Event<String>> = _notification

    private var _listenLaterDataList = mutableListOf<ListenLaterItemDomainModel>()
    val listenLaterData:List<ListenLaterItemDomainModel> = _listenLaterDataList

    fun setCurrentShortType(type:SortType){
        currentShortType = type
        loadList()
    }

    fun loadList() {
        viewModelScope.launch {
            _requestStatus.value = Event(Started)

            val data = when(currentShortType){
                SortType.LatestFirst -> {
                    withContext(coroutineContext) { repository.getBooksInLIFO() }
                }

                SortType.OldestFirst -> {
                    withContext(coroutineContext) { repository.getBooksInFIFO() }
                }

                SortType.ShortestFirst -> {
                    withContext(coroutineContext) { repository.getBooksInOrderOfLength() }
                }
            }

            if(!data.isNullOrEmpty()){
                _requestStatus.value = Event(Success(data))
                _listenLaterDataList.clear()
                _listenLaterDataList.addAll(data)
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

            loadList()
        }
    }

    fun export(pfd: ParcelFileDescriptor){
        viewModelScope.launch {
            val result = exportUserData.exportToPath(pfd)

            result.collect { isSuccess ->
                Timber.d("Value is $isSuccess")
                if (isSuccess){
                    _notification.value = Event(StringUtility.DATA_EXPORT_SUCCESS)
                }else{
                    _notification.value = Event(StringUtility.DATA_EXPORT_FAILED)
                }
            }
        }
    }

    fun import(pfd: ParcelFileDescriptor){
        viewModelScope.launch {
            val result = importUserData.fromFile(pfd)

            result.collect { isSuccess ->
                Timber.d("Value is $isSuccess")
                if (isSuccess){
                    loadList()
                    _notification.value = Event(StringUtility.DATA_IMPORT_SUCCESS)
                }else{
                    _notification.value = Event(StringUtility.DATA_IMPORT_FAILED)
                }
            }
        }
    }

    //cancel the job when viewmodel is not longer in use
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}