package com.allsoftdroid.audiobook.presentation.utils

import androidx.test.runner.screenshot.Screenshot
import timber.log.Timber
import java.io.IOException

object TakeScreenshotUtils {
    fun takeScreenshot(parentFolderPath: String = "", screenShotName: String) {
        Timber.d("Taking screenshot of '$screenShotName'")
        val screenCapture = Screenshot.capture()
        val processors = setOf(ScreenCaptureProcessor(parentFolderPath))
        try {
            screenCapture.apply {
                name = screenShotName
                process(processors)
            }
            Timber.d("Screenshot taken")
        } catch (ex: IOException) {
            Timber.d("Could not take the screenshot: $ex")
        }
    }
}