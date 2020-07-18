package com.allsoftdroid.feature.book_details.data.repository

import com.allsoftdroid.common.test.wrapEspressoIdlingResource
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.asTrackDomainModel
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber

class TrackListRepositoryImpl (
    private val metadataDao : MetadataDao,
    private val bookId: String
) : ITrackListRepository{

    private lateinit var _audioBookTrackList2 : Flow<List<AudioBookTrackDomainModel>>

    @ExperimentalCoroutinesApi
    override suspend fun loadTrackListData(format: TrackFormat) : Flow<List<AudioBookTrackDomainModel>> {
        wrapEspressoIdlingResource {
            Timber.d("Track format :${format}")
            _audioBookTrackList2 = when(format){
                is TrackFormat.FormatBP64 -> {
                    metadataDao.getTrackDetails(bookId,formatContains = "64")
                        .map { it.asTrackDomainModel() }
                        .flowOn(Dispatchers.IO)
                }
                is TrackFormat.FormatBP128 -> {
                    metadataDao.getTrackDetails(bookId,formatContains = "128")
                        .map { it.asTrackDomainModel() }
                        .flowOn(Dispatchers.IO)
                }
                else ->{
                    metadataDao.getTrackDetailsVBR(bookId)
                        .map { it.asTrackDomainModel() }
                        .flowOn(Dispatchers.IO)
                }
            }

            return _audioBookTrackList2
        }
    }
}