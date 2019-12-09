package com.allsoftdroid.feature.book_details.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
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

    private val trackLoadEvent = MutableLiveData<Event<Any>>()

    private var _audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
            = Transformations.switchMap(trackLoadEvent){
        Transformations.map(
            metadataDao.getTrackDetails(bookId,formatContains = "64")
        ){
            it.asTrackDomainModel()
        }
    }

    private val audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
    get() = _audioBookTrackList


    override fun getBookId() = bookId

    /***
     * track network response for  completion and started
     */

    private var _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    override suspend fun loadMetadata() {
        withContext(Dispatchers.IO){
            Timber.i("Starting network call")
            Timber.i("Loading for id:$bookId")
            metadataDataSource.getMetadata(bookId).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.i("Failure occur")
                    _response.value="NULL"
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), GetAudioBookMetadataResponse::class.java)

                    result?.let {

                        Timber.i("Response file size: ${result.item_size}")
                        _response.value = result.item_size

                        Timber.i("Size:${result.metadata.title}")

                        /**
                         * Run with application scope
                         * TODO: better way?
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

    override suspend fun loadTrackListData() {
        trackLoadEvent.value = Event(Unit)
        Timber.d("Metadata loaded check:${response.value}")
    }

    override fun getTrackList() = audioBookTrackList
}