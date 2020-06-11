package com.allsoftdroid.audiobook.feature_downloader.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import com.allsoftdroid.audiobook.feature_downloader.R
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import timber.log.Timber

object DownloadNotificationUtils {

    //notification id
    private const val NOTIFY_ID = 2
    private const val NOTIFICATION_CHANNEL = "audio_book_download_channel"

    @SuppressLint("NewApi")
    fun sendNotification(applicationContext: Context,filename:String,currentQueueSize:Int,progress:Long) {

        val currentTrack = "Downloading: ${getNormalizedText(filename, 20)} ($progress%)"
        var details = "Pending ($currentQueueSize)"
        var ongoing=true
        if (currentQueueSize<=1){
            if (progress.toInt() >= 97) details = "Completed"
            ongoing = false
        }


        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {

            val name = "AudioBook downloads"
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                description = "AudioBook downloads channel"
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }

        var icon = R.drawable.ic_download_multiple_black

        when (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                icon = R.drawable.ic_download_multiple_black
            } // Night mode is not active, we're using the light theme

            Configuration.UI_MODE_NIGHT_YES -> {
                icon = R.drawable.ic_download_multiple
            } // Night mode is active, we're using dark theme
        }

        val notification = NotificationCompat.Builder(applicationContext,
            NOTIFICATION_CHANNEL)
            .setSmallIcon(icon)
            .setContentTitle(currentTrack)
            .setContentText(details)
            .setOngoing(ongoing)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setContentIntent(getContentIntent(applicationContext))

        notificationManager.notify(NOTIFY_ID,notification.build())

        Timber.d("Notification sent for $filename")
    }

    private fun getContentIntent(applicationContext: Context): PendingIntent {
        val contentIntent = applicationContext.packageManager.getLaunchIntentForPackage("com.allsoftdroid.audiobook")
        contentIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(applicationContext, 0, contentIntent, 0)
    }
}