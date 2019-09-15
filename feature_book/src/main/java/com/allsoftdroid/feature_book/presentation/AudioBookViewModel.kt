package com.allsoftdroid.feature_book.presentation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import kotlinx.coroutines.launch

class AudioBookViewModel(
    private val getAlbumListUseCase: GetAudioBookListUsecase, application: Application
) : ViewModel() {

    var _audioBooks = MutableLiveData<List<AudioBookDomainModel>>()
    val audioBooks:LiveData<List<AudioBookDomainModel>>
    get() = _audioBooks

    init {
        getAlbumList()
    }


    private fun getAlbumList() {
        viewModelScope.launch {
            getAlbumListUseCase.execute().also {
                _audioBooks.value = getAlbumListUseCase.getAudioBook().value
            }
        }
    }
}
