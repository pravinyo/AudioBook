package com.allsoftdroid.feature_book.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.feature_book.data.databaseExtension.SaveBookListInDatabase
import com.allsoftdroid.feature_book.data.databaseExtension.asBookDomainModel
import com.allsoftdroid.feature_book.data.model.AudioBookDataModel
import com.allsoftdroid.feature_book.data.network.Utils
import com.allsoftdroid.feature_book.data.network.response.GetAudioBooksResponse
import com.allsoftdroid.feature_book.data.network.service.ArchiveBooksApi
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    val audioBook : LiveData<List<AudioBookDomainModel>>
    get() = _audioBooks

    /***
     * track network response for  completion and started
     */
    private var _response = MutableLiveData<Int>()
    val response: LiveData<Int>
        get() = _response


    /**
     * Using coroutine to handle the execution and update the network response and load database
     * It request the content of type books for new updates
     */
    override suspend fun fetchBookList(page:Int) {
        withContext(Dispatchers.IO) {
            Timber.i("Starting network call")

            ArchiveBooksApi.RETROFIT_SERVICE.getAudioBooks(
                page = page,
                rowCount = Utils.Books.DEFAULT_ROW_COUNT
            ).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.i("Failure occur")
                    _response.value = 0
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val gson = Gson()
                    val result =
                        gson.fromJson(response.body(), GetAudioBooksResponse::class.java)

                    Timber.i("Response got: ${result.response.docs[0].title}")

                    result?.response?.docs?.let {
                        _response.value = result.response.docs.size
                        Timber.i("Size:${result.response.docs.size}")

                        /**
                         * Since we have data, we can independently save it to database
                         * It uses entire application scope
                         */
                        GlobalScope.launch {
                            saveToDatabase(result.response.docs)
                        }
                    }
                }
            })
        }
    }

    private suspend fun saveToDatabase(list:List<AudioBookDataModel>){
        Timber.i("Saving to DB")
        if (list.isNotEmpty()){
            Timber.i("List is not empty saving to Database")
            SaveBookListInDatabase.setup(bookDao)
                .addData(data = list)
                .execute()
        }else{
            Timber.i("List is empty")
        }
    }


    override fun getAudioBooks() =  this.audioBook
}

