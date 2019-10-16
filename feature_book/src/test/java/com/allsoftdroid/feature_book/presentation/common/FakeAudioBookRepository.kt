package com.allsoftdroid.feature_book.presentation.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository

class FakeAudioBookRepository : AudioBookRepository{

    private var response = MutableLiveData<Event<Any>>()

    override fun onError() = response

    private var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()

    override suspend fun searchAudioBooks() {
        val list = ArrayList<AudioBookDomainModel>()
        list.add(AudioBookDomainModel("1","Title","creator","2019"))

        audioBooks.value = list
        response.value = Event("Success")
    }

    override fun getAudioBooks()= audioBooks

    fun setFailure(){
        response.value = Event(Throwable("Error"))
    }
}