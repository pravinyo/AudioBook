package com.allsoftdroid.audiobook.feature_settings.utils

import android.net.Uri
import android.os.Environment
import timber.log.Timber

object FilePath {

    fun getPath(folderUri: Uri): String {

        return folderUri.toString()
            .replace("%3A",":")
            .replace("%2F","/")
            .split(":").last()
    }

    fun subDirectory(path:String):String{
        val root = Environment.getExternalStorageDirectory().path
        Timber.d("Path root: $root")

        val folder = path.substring(root.length+1)
        Timber.d("Path returned: $folder")

        return folder
    }
}