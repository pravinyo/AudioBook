package com.allsoftdroid.feature.book_details.presentation.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository

class FakeTrackListRepository : ITrackListRepository {

    private var trackList = MutableLiveData<List<AudioBookTrackDomainModel>>()

    override suspend fun loadTrackListData(format: TrackFormat) {
        val tracks = mutableListOf<AudioBookTrackDomainModel>()

        tracks.add(AudioBookTrackDomainModel(filename = "sample.mp3",title = "sample track",trackNumber = 0,trackAlbum = "Intro",
            length = "1",format = "64KB",size = "2MB",trackId = "1"))

        tracks.add(AudioBookTrackDomainModel(filename = "sample2.mp3",title = "sample2 track",trackNumber = 1,trackAlbum = "Intro",
            length = "1",format = "64KB",size = "2MB",trackId = "2"))

        trackList.value = tracks
    }

    override fun getTrackList(): LiveData<List<AudioBookTrackDomainModel>> {
        return trackList
    }
}