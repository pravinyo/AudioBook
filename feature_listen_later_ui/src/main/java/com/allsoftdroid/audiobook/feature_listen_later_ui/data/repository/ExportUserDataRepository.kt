package com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository

import android.os.ParcelFileDescriptor
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.BookMarkDataItem
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IExportUserDataRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.CommonUtility
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ExportUserDataRepository(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    IExportUserDataRepository {

    private var dataToExport: String? = null

    private fun prepareUserData(data: List<BookMarkDataItem>) {
        val jsonArray = JSONArray()
        data.forEach {
            val jsonObject = JSONObject()
            jsonObject.put("bookId",it.bookId)
            jsonObject.put("bookName",it.bookName)
            jsonObject.put("bookAuthor",it.bookAuthor)
            jsonObject.put("duration",it.duration)
            jsonObject.put("timeStamp",it.timeStamp)

            jsonArray.put(jsonObject)
        }

        val intermediateData = jsonArray.toString()
        Timber.d("Data Before is :$intermediateData")
        dataToExport = CommonUtility.encrypt(intermediateData,CommonUtility.defaultSecretKey)
        Timber.d("Data After is :$dataToExport")
    }

    override suspend fun toFile(path: String, data: List<BookMarkDataItem>) {
        withContext(dispatcher){
            prepareUserData(data)
            val file = "Export_${System.currentTimeMillis()}.data"
            val exportFile = File(path,file)

            with(exportFile){
                bufferedWriter().use {out ->
                    dataToExport?.let { out.write(it) }
                }
            }
        }
    }

    override suspend fun toFile(pfd: ParcelFileDescriptor, data: List<BookMarkDataItem>) {
        withContext(dispatcher){
            prepareUserData(data)

            val fileStream = FileOutputStream(pfd.fileDescriptor)
            fileStream.bufferedWriter().use {out ->
                dataToExport?.let { out.write(it) }
            }
        }
    }
}