package com.allsoftdroid.common.base.network

import android.app.Activity
import android.content.Context
import android.os.Environment
import com.allsoftdroid.common.R

class ArchiveUtils {
    companion object{

        private const val BASE_DOWNLOAD_URL = "https://archive.org/download"
        private const val BASE_IMAGE_URL = "https://archive.org/services/img"

        /**
         * This function generated the file path on the remote server
         * @param filename unique filename on the server
         * @return complete file path on the remote server
         */
        fun getRemoteFilePath(filename: String,identifier:String):String{
            return "$BASE_DOWNLOAD_URL/$identifier/$filename"
        }

        fun getThumbnail(imageId: String?) = "$BASE_IMAGE_URL/$imageId/"

        fun getLocalSavePath(path:String) = "/AudioBooks/$path/"

        fun setDownloadsRootFolder(context: Activity, root:String){
            val sharedPref = context.getPreferences(Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(context.getString(R.string.downloads_root_directory_key), root)
                commit()
            }
        }

        fun getDownloadsRootFolder(context: Activity): String {
            val default = Environment.DIRECTORY_DOWNLOADS
            val sharedPref = context.getPreferences(Context.MODE_PRIVATE)

            return sharedPref.getString(context.getString(R.string.downloads_root_directory_key),default)?:default
        }
    }
}