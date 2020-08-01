package com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository

import android.os.ParcelFileDescriptor
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.BookMarkDataItem
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.repository.IImportUserDataRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.utils.CommonUtility
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

class ImportUserDataRepository(private val dispatcher:CoroutineDispatcher = Dispatchers.IO) :
    IImportUserDataRepository {

    private fun getParsedList(jsonString:String?):List<BookMarkDataItem> {
        return if (jsonString == null) emptyList()
        else{
            val returnList = mutableListOf<BookMarkDataItem>()
            val jsonArray = JSONArray(jsonString)
            Timber.d("json array is $jsonArray")
            Timber.d("json array length is ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()){
                val item = jsonArray[i] as JSONObject
                returnList.add(
                    BookMarkDataItem(
                        bookId = item.getString("bookId"),
                        bookName = item.getString("bookName"),
                        bookAuthor = item.getString("bookAuthor"),
                        duration = item.getString("duration"),
                        timeStamp = item.getString("timeStamp")
                    ))
            }
            Timber.d("return list is $returnList")
            returnList
        }
    }

    override suspend fun fromFile(path: String): List<BookMarkDataItem> {
        val jsonString:String? = withContext(dispatcher) {
            getContent(path)
        }

        Timber.d("Json string received is : $jsonString")
        return getParsedList(jsonString)
    }

    override suspend fun fromFile(pfd: ParcelFileDescriptor): List<BookMarkDataItem> {
        val jsonString:String? = withContext(dispatcher) {
            getContent(pfd)
        }

        Timber.d("Json string received is : $jsonString")

        return getParsedList(jsonString)
    }

    private fun getContent(pfd: ParcelFileDescriptor):String?{
        val result: String?

        val fileStream = FileInputStream(pfd.fileDescriptor)
        val encryptedData = fileStream.bufferedReader().use { input -> input.readText() }
        result = CommonUtility.decryptWithAES(CommonUtility.defaultSecretKey,encryptedData)

        return result
    }

    private fun getContent(path: String):String?{
        var result:String?=null

        return try {
            val dir = File(path)

            if (dir.isDirectory){
                val files = dir.listFiles()
                files?.let {fileList ->
                    fileList.sortDescending()

                    Timber.d("File is ${fileList.first().name}")
                    val encryptedData = File(path,fileList.first().name).bufferedReader().use { input -> input.readText() }
                    Timber.d("Encrypted data is :$encryptedData")
                    result = CommonUtility.decryptWithAES(CommonUtility.defaultSecretKey,encryptedData)
                    if(result==null){
                        Timber.d("Decryption failed")
                    }
                    Timber.d("data is :$result")
                }
            }
            result
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }
}