package com.allsoftdroid.feature.book_details.Utility

class Utils {
    companion object{

        val BASE = "https://archive.org/download"

        /**
         * This function generated the file path on the remote server
         * @param filename unique filename on the server
         * @return complete file path on the remote server
         */
        fun getRemoteFilePath(filename: String,identifier:String):String{
            return "$BASE/$identifier/$filename"
        }
    }
}