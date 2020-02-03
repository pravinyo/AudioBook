package com.allsoftdroid.audiobook.feature_downloader

import android.content.Context
import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allsoftdroid.audiobook.feature_downloader.utils.downloadUtils
import com.allsoftdroid.common.base.extension.Event
import kotlinx.coroutines.*

class DownloadViewModel : ViewModel(){
    /**
     * cancelling this job cancels all the job started by this viewmodel
     */
    private val viewModelJob: CompletableJob  = SupervisorJob()

    private var _downloadingList = MutableLiveData<Event<Cursor>>()
    val downloadingList : LiveData<Event<Cursor>> = _downloadingList

    /**
     * main scope for all coroutine launched by viewmodel
     */
    private val viewModelScope = CoroutineScope(viewModelJob) +Dispatchers.Main

    fun refresh(context:Context){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                downloadUtils.LogAllLocalData(context)
                val cursor = downloadUtils.getCustomCursor(context)

                withContext(Dispatchers.Main){
                    _downloadingList.value = Event(cursor)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}