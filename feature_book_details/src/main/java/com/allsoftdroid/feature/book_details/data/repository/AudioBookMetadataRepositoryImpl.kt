package com.allsoftdroid.feature.book_details.data.repository

import android.os.AsyncTask
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import com.allsoftdroid.feature.book_details.data.databaseExtension.asMetadataDomainModel
import com.allsoftdroid.feature.book_details.data.databaseExtension.asTrackDomainModel
import com.allsoftdroid.feature.book_details.data.model.toDatabaseModel
import com.allsoftdroid.feature.book_details.data.network.response.GetAudioBookMetadataResponse
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataApi
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

//TODO: make using coroutine replace async with coroutine or rxjava
class AudioBookMetadataRepositoryImpl(private val metadataDao : MetadataDao,@NonNull private val bookId: String) : AudioBookMetadataRepository{

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

    val audioBookTrackList : LiveData<List<AudioBookTrackDomainModel>>
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
            ArchiveMetadataApi.RETROFIT_SERVICE.getMetadata(bookId).enqueue(object : Callback<String> {
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
                        LoadDB(metadataDao).execute(result)
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

    /**
     * Load the database with the provided list of Book Instance
     * It first clears old Books records  from the DB  and reload fresh content
     */
    private class LoadDB(private val metadataDao: MetadataDao) : AsyncTask<GetAudioBookMetadataResponse, Unit, Int>(){

        override fun doInBackground(vararg params: GetAudioBookMetadataResponse?):Int {

            //safe check performed
            val result = params[0] ?: return 0

            val metadata = DatabaseMetadataEntity(
                identifier = result.metadata.identifier,
                creator = result.metadata.creator,
                date = result.metadata.date,
                description = result.metadata.description,
                licenseUrl = result.metadata.licenseurl,
                tag = result.metadata.subject,
                title = result.metadata.title,
                release_year = result.metadata.publicdate,
                runtime = result.metadata.runtime?:"NA"
            )

            val trackList = ArrayList<DatabaseTrackEntity>()

            //scan the list and build the new track list to be inserted in the database
            val tracks = result.files.filter {
                it.format.toLowerCase(Locale.getDefault()).contains("mp3")
            }

            for(track in tracks){
                trackList.add(track.toDatabaseModel(metadata.identifier))
            }

            val album = DatabaseAlbumEntity(
                identifier = result.metadata.identifier,
                albumName = tracks[0].album?:"NA",
                creator = metadata.creator?:"NA"
            )


            metadataDao.insertMetadata(metadata)
            Timber.d("Metadata loaded")
            metadataDao.insertAlbum(album)
            Timber.d("Album details loaded")
            metadataDao.insertAllTracks(trackList)
            Timber.d("#${trackList.size} tracks loaded in the DB")

            val list = metadataDao.getTrackDetails(metadata_id = metadata.identifier).value

            list?.forEach {
                Timber.d(it.trackAlbum_id)
                Timber.d(it.trackTitle)
            }

            return list?.size?:0
        }

        /**
         * For debugging/Logging purpose
         */
        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            Timber.i("all new data count $result")
        }
    }

}