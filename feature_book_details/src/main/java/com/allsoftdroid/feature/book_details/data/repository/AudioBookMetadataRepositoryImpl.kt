package com.allsoftdroid.feature.book_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.databaseExtension.asMetadataDomainModel
import com.allsoftdroid.feature.book_details.data.databaseExtension.asTrackDomainModel
import com.allsoftdroid.feature.book_details.data.network.response.GetAudioBookMetadataResponse
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataService
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import com.allsoftdroid.feature.book_details.utils.NetworkState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class AudioBookMetadataRepositoryImpl(
    private val metadataDao : MetadataDao,
    private val bookId: String,
    private val metadataDataSource : ArchiveMetadataService,
    private val saveInDatabase: SaveInDatabase<MetadataDao,SaveMetadataInDatabase>
) : AudioBookMetadataRepository{

    /**
     * Books type live data is fetched from the database and notify observer for any change in data.
     * Books count at restricted to {@link BOOK_LIMIT}.
     * Data are converted to Domain model type instance
     */
    private var _audioBookMetadata : LiveData<AudioBookMetadataDomainModel> = Transformations.map(
        metadataDao.getMetadata(bookId)
    ){
        it?.asMetadataDomainModel()
    }

    private val audioBookMetadata : LiveData<AudioBookMetadataDomainModel>
        get() = _audioBookMetadata

    private val trackLoadEvent = MutableLiveData<Event<TrackFormat>>()

    private var _audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
            = Transformations.switchMap(trackLoadEvent){
        Transformations.map(
            metadataDao.getTrackDetails(bookId,formatContains = "64")
        ){
            it.asTrackDomainModel()
        }
    }

    private var _audioBookTrackList2 : LiveData<List<AudioBookTrackDomainModel>>
            = Transformations.switchMap(trackLoadEvent){event ->
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

    private val audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
    get() = _audioBookTrackList2


    override fun getBookId() = bookId

    /***
     * track network response for  completion and started
     */

    private var _networkResponse = Variable(Event(NetworkState.LOADING))

    override suspend fun loadMetadata() {
        withContext(Dispatchers.IO){
            _networkResponse.value = Event(NetworkState.LOADING)

            Timber.i("Starting network call")
            Timber.i("Loading for id:$bookId")

            metadataDataSource.getMetadata(bookId).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.i("Failure occur:${t.message}")
                    _networkResponse.value= Event(NetworkState.ERROR)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), GetAudioBookMetadataResponse::class.java)

                    result?.let {

                        Timber.i("Response file size: ${result.item_size}")
                        _networkResponse.value = Event(NetworkState.COMPLETED)

                        Timber.i("Size:${result.metadata.title}")

                        /**
                         * Run with application scope
                         */
                        GlobalScope.launch {
                            saveInDatabase
                                .addData(result)
                                .execute()
                        }
                    }
                }
            })
        }
    }

    override fun getMetadata() = audioBookMetadata

    override suspend fun loadTrackListData(format: TrackFormat) {
        trackLoadEvent.value = Event(format)
        Timber.d("Track format :${format}")
    }

    override fun getTrackList() = audioBookTrackList

    override fun networkResponse(): Variable<Event<NetworkState>> = _networkResponse
}