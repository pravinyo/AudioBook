package com.allsoftdroid.audiobook.feature_listen_later_ui.presentation

import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository.ExportUserDataRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository.ImportUserDataRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.SUPER_VISOR_JOB
import com.allsoftdroid.audiobook.feature_listen_later_ui.di.FeatureListenLaterModule.VIEW_MODEL_SCOPE
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.*
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IListenLaterRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase.ExportUserData
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.usecase.ImportUserData
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.SortType
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.listenLaterDB.ListenLaterDao
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber
import java.io.File

class ListenLaterViewModel(private val repository: IListenLaterRepository) : ViewModel(), KoinComponent {
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob by inject(named(name = SUPER_VISOR_JOB))

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope : CoroutineScope by inject(named(name = VIEW_MODEL_SCOPE))

    private val listenLaterDao : ListenLaterDao by inject(named(name = FeatureListenLaterModule.BEAN_NAME))

    private var currentShortType = SortType.LatestFirst

    private var _requestStatus = MutableLiveData<Event<RequestStatus>>()
    val requestStatus : LiveData<Event<RequestStatus>> = _requestStatus

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
            val export = ExportUserData(listenLaterDao, ExportUserDataRepository())

            val result = export.exportToPath(pfd)

            result.collect { value ->
                Timber.d("Value is $value")
            }
        }
    }

    fun export(){
        viewModelScope.launch {
            val export = ExportUserData(listenLaterDao, ExportUserDataRepository())
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state){
                if (Build.VERSION.SDK_INT >= 23){
                    val sdcard = Environment.getExternalStorageDirectory()
                    val dir = File(sdcard.absolutePath+"/AudioBookAppExport/")
                    dir.mkdir()

                    Timber.d("Path to export is ${dir.absolutePath}")
                    val result = export.exportToPath(dir.absolutePath)

                    result.collect { value ->
                        Timber.d("Value is $value")
                    }
                }
            }
        }
    }


    fun import(pfd: ParcelFileDescriptor){
        viewModelScope.launch {
            val import = ImportUserData(listenLaterDao, ImportUserDataRepository())

            val result = import.fromFile(pfd)

            result.collect { value ->
                loadList()
                Timber.d("Value is $value")
            }
        }
    }

    fun import(){
        viewModelScope.launch {
            val import = ImportUserData(listenLaterDao, ImportUserDataRepository())
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state){
                val sdcard = Environment.getExternalStorageDirectory()
                val dir = File(sdcard.absolutePath+"/AudioBookAppExport/")
                dir.mkdir()

                Timber.d("Path to export is ${dir.absolutePath}")
                val result = import.fromFile(path = dir.absolutePath)

                result.collect { value ->
                    loadList()
                    Timber.d("Value is $value")
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