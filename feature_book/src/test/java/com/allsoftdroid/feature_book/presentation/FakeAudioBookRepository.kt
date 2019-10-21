package com.allsoftdroid.feature_book.presentation

import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository

class FakeAudioBookRepository : AudioBookRepository{
    private var audioBooks = MutableLiveData<List<AudioBookDomainModel>>()

    override suspend fun searchAudioBooks(page: Int) {
        val list = ArrayList<AudioBookDomainModel>()
        list.add(AudioBookDomainModel("1","Title","creator","2019"))

        audioBooks.value = list
    }

    override fun getAudioBooks()= audioBooks
}