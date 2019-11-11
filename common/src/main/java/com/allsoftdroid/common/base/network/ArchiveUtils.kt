package com.allsoftdroid.common.base.network

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
    }
}