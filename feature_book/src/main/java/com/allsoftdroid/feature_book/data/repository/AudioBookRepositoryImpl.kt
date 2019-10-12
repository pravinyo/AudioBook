package com.allsoftdroid.feature_book.data.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.feature_book.data.databaseExtension.asBookDomainModel
import com.allsoftdroid.feature_book.data.model.AudioBookDataModel
import com.allsoftdroid.feature_book.data.model.toDatabaseModel
import com.allsoftdroid.feature_book.data.network.response.GetAudioBooksResponse
import com.allsoftdroid.feature_book.data.network.service.ArchiveBooksApi
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class AudioBookRepositoryImpl(private val bookDao : AudioBookDao) : AudioBookRepository {
    /**
     * Books type live data is fetched from the database and notify observer for any change in data.
     * Books count at restricted to {@link BOOK_LIMIT}.
     * Data are converted to Domain model type instance
     */
    private var _audioBooks : LiveData<List<AudioBookDomainModel>> = Transformations.map(
        bookDao.getBooks()
    ){
        it.asBookDomainModel()
    }

    private val audioBook : LiveData<List<AudioBookDomainModel>>
    get() = _audioBooks

    /***
     * track error response for  completion and started
     */
    private var _errorResponse = MutableLiveData<Event<Any>>()
    private val errorResponse: LiveData<Event<Any>>
        get() = _errorResponse

    /**
     * for debugging purpose
     * Track database count for Book type data in table
     */
    private var _db_count = MutableLiveData<Int>()
    val db_count : LiveData<Int>
        get() = _db_count



    /**
     * Using coroutine to handle the execution and update the network response and load database
     * It request the content of type books for new updates
     */
    override suspend fun searchAudioBooks() {

        withContext(Dispatchers.IO){
            Timber.i("Starting network call")

            ArchiveBooksApi.RETROFIT_SERVICE.getAudioBooks().enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.i("Failure occur")
                    _errorResponse.value = Event(t)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result = gson.fromJson(response.body(), GetAudioBooksResponse::class.java)

                    Timber.i("Response got: ${result.response.docs[0].title}")

                    result?.response?.docs?.let {
                        _errorResponse.value = Event("Success")
                        Timber.i("Size:${result.response.docs.size}")
                        Timber.i("First:${result.response.docs[0].title}")
                        Timber.i("Creator:${result.response.docs[0].creator}")
                        Timber.i("identifier:${result.response.docs[0].identifier}")
                        Timber.i("date:${result.response.docs[0].date}")
                        LoadDB(bookDao).execute(result.response.docs)
                    }
                }
            })
        }
    }

    override fun getAudioBooks() =  audioBook

    override fun onError() = errorResponse

    /**
     * Load the database with the provided list of Book Instance
     * It first clears old Books records  from the DB  and reload fresh content
     */
    private class LoadDB(private val bookDao: AudioBookDao) : AsyncTask<List<AudioBookDataModel>, Unit, Int>(){

        override fun doInBackground(vararg params: List<AudioBookDataModel>?):Int {

            //safe check performed
            val result = params[0] ?: return 0

            val bookList : MutableList<DatabaseAudioBook> = ArrayList()

            //scan the list and build the new to be inserted in the database
            for(element in result){
                val book: AudioBookDataModel = element

                Timber.i(book.identifier)
                Timber.i(book.title)

                bookList.add(book.toDatabaseModel())
            }

            //remove already existing data
            val deleted= bookDao.deleteAll()
            Timber.i("data deleted $deleted")


            //Insert new Fresh data in the database
            bookDao.insertAllList(bookList)
            Timber.i("data loaded ${bookList.size}")


            val inserted = bookDao.getBooks()
            val count = inserted.value?.size
            Timber.i("data fetching from DB: $count")

            val entry = 10
            val sampleBook = bookDao.getBookBy(bookList[entry].identifier)
            Timber.i("Data check for #$entry entry : ${sampleBook.title}")

            //return count for logging purpose only
            return count?:0
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

