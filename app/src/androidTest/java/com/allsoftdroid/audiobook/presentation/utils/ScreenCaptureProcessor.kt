package com.allsoftdroid.audiobook.presentation.utils

import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor
import java.io.File

class ScreenCaptureProcessor(parentFolderPath: String) : BasicScreenCaptureProcessor() {

    init {
        this.mDefaultScreenshotPath = File(
            File(
                getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                "audioBook_test_folder"
            ).absolutePath,
            "screenshots/$parentFolderPath"
        )
    }

    override fun getFilename(prefix: String): String = prefix
}