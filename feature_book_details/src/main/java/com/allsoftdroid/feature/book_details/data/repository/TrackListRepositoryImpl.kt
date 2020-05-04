package com.allsoftdroid.feature.book_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.test.wrapEspressoIdlingResource
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.asTrackDomainModel
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import timber.log.Timber

class TrackListRepositoryImpl (
    private val metadataDao : MetadataDao,
    private val bookId: String
) : ITrackListRepository{

    private val trackLoadEvent = MutableLiveData<Event<TrackFormat>>()

    private var _audioBookTrackList2 : LiveData<List<AudioBookTrackDomainModel>>
            = Transformations.switchMap(trackLoadEvent){ event ->
        wrapEspressoIdlingResource {
            event.getContentIfNotHandled()?.let {format ->
                when(format){
                    is TrackFormat.FormatBP64 -> {
                        Transformations.map(
                            metadataDao.getTrackDetails(bookId,formatContains = "64")
                        ){
                            it.asTrackDomainModel()
                        }
                    }
                    is TrackFormat.FormatBP128 -> {
                        Transformations.map(
                            metadataDao.getTrackDetails(bookId,formatContains = "128")
                        ){
                            it.asTrackDomainModel()
                        }
                    }
                    else ->{
                        Transformations.map(
                            metadataDao.getTrackDetailsVBR(bookId)
                        ){
                            it.asTrackDomainModel()
                        }
                    }
                }
            }
        }
    }

    private val audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
        get() = _audioBookTrackList2

    override suspend fun loadTrackListData(format: TrackFormat) {
        wrapEspressoIdlingResource {
            trackLoadEvent.value = Event(format)
            Timber.d("Track format :${format}")
        }
    }

    override fun getTrackList() = audioBookTrackList
}