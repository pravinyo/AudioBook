package com.allsoftdroid.audiobook.services.audio.utils

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.network.ArchiveUtils
import timber.log.Timber
import java.io.File
import java.util.*

class LocalFilesForBook(private val app:Application) {

    private fun getDownloadedFilesList(bookId:String):List<String>?{
        val directory = Environment.getExternalStoragePublicDirectory(ArchiveUtils.getDownloadsRootFolder(context = app))

        val path = directory.toString() + ArchiveUtils.getLocalSavePath(bookId)
        Timber.d("Path: $path")
        val dir = File(path)

        return dir.listFiles()?.let {
            it.map { file ->
                file.absolutePath
            }
        }
    }

    fun getListHavingOnlineAndOfflineUrl(bookId: String,trackList:List<AudioPlayListItem>):List<Uri>{
        val localList = getDownloadedFilesList(bookId)
        Timber.d("Local Files: ${localList.toString()}")

        val returnList =  trackList.map {
            ArchiveUtils.getRemoteFilePath(it.filename,bookId).toUri()
        }.toMutableList()

        Timber.d("Remote Files: $returnList")

        //return original list if no file found locally
        if (localList != null) {

            for(localFile in localList){
                val name = localFile.split("/").last()
                for (i in trackList.indices){
                    if(trackList[i].filename.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT)) {
                        returnList.removeAt(i)
                        returnList.add(i,Uri.parse(localFile))
                    }
                }
            }
        }

        Timber.d("Merged Results: $returnList")
        return returnList
    }
}