package com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository

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
import java.lang.Exception

class ImportUserDataRepository(private val dispatcher:CoroutineDispatcher = Dispatchers.IO) :
    IImportUserDataRepository {

    private val secretKey: String = "662ede816988e58fb6d057d9d85605e0"

    override suspend fun fromFile(path: String): List<BookMarkDataItem> {
        val jsonString:String? = withContext(dispatcher) {
            getContent(path)
        }

        Timber.d("Json string received is : $jsonString")

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
                    result = CommonUtility.decryptWithAES(secretKey,encryptedData)
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