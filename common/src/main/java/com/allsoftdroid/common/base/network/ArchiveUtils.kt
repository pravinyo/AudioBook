package com.allsoftdroid.common.base.network

import android.app.Application
import android.content.Context
import android.os.Environment
import com.allsoftdroid.common.R

class ArchiveUtils {
    companion object{

        private const val BASE_DOWNLOAD_URL = "https://archive.org/download"
        private const val BASE_IMAGE_URL = "https://archive.org/services/img"
        const val AppFolderName = "AudioBooks"

        /**
         * This function generated the file path on the remote server
         * @param filename unique filename on the server
         * @return complete file path on the remote server
         */
        fun getRemoteFilePath(filename: String,identifier:String):String{
            return "$BASE_DOWNLOAD_URL/$identifier/$filename"
        }

        fun getThumbnail(imageId: String?) = "$BASE_IMAGE_URL/$imageId/"

        fun getLocalSavePath(bookId:String) = "/$AppFolderName/$bookId/"

        fun setDownloadsRootFolder(context: Application, root:String){
            val sharedPref = context.getSharedPreferences(context.getString(R.string.downloads_path),Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(context.getString(R.string.downloads_root_directory_key), root)
                commit()
            }
        }

        fun getDownloadsRootFolder(context: Application): String {
            val default = Environment.DIRECTORY_DOWNLOADS
            val sharedPref = context.getSharedPreferences(context.getString(R.string.downloads_path),Context.MODE_PRIVATE)

            return sharedPref.getString(context.getString(R.string.downloads_root_directory_key),default)?:default
        }
    }
}