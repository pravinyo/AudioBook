package com.allsoftdroid.audiobook.services.audio

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.utils.ImageUtils


class NotificationUtils {

    companion object{

        //notification id
        private const val NOTIFY_ID = 1
        private const val NOTIFICATION_CHANNEL = "audio_book_music_player_channel"

        @SuppressLint("NewApi")
        fun sendNotification(isAudioPlaying:Boolean,currentAudioPos:Int,service: AudioService,applicationContext: Context,trackTitle:String,bookId: String,bookName:String) {

            val collapsedView = RemoteViews(applicationContext.packageName, R.layout.notification_mini_player_collapsed)
            val playPauseIcon = if (isAudioPlaying) R.drawable.ic_play_arrow_black_24dp else R.drawable.ic_pause_black_24dp

            collapsedView.setTextViewText(R.id.notification_book_name,bookName)
            collapsedView.setTextViewText(R.id.notification_track_name,trackTitle)
            collapsedView.setImageViewResource(R.id.image_notification_playpause,playPauseIcon)

            val drawable = CreateImageOverlay
                .with(applicationContext)
                .buildOverlay(front = R.drawable.ic_book_play,back = R.drawable.gradiant_background)

            val roundImage  = ImageUtils.getCircleBitmap(drawable.toBitmap())

            collapsedView.setImageViewBitmap(R.id.notification_track_thumbnail,roundImage)

            collapsedView.setOnClickPendingIntent(
                R.id.image_notification_prev,
                NotificationPlayerEventBroadcastReceiver.newPendingIntent(
                    context = applicationContext,
                    action = AudioService.PREVIOUS,
                    requestCode = AudioService.ACTION_PREVIOUS))

            collapsedView.setOnClickPendingIntent(R.id.image_notification_next,
                NotificationPlayerEventBroadcastReceiver.newPendingIntent(
                    context = applicationContext,
                    action = AudioService.NEXT,
                    requestCode = AudioService.ACTION_NEXT
                ))

            collapsedView.setOnClickPendingIntent(R.id.image_notification_playpause,
                NotificationPlayerEventBroadcastReceiver.newPendingIntent(
                    context = applicationContext,
                    action = if(isAudioPlaying) AudioService.PAUSE else AudioService.PLAY,
                    requestCode = AudioService.ACTION_PLAY_PAUSE
                ))

            var notifyWhen = 0L
            var showWhen = false
            var usesChronometer = false
            val ongoing = true
            if (isAudioPlaying) {
                notifyWhen = System.currentTimeMillis() - (currentAudioPos)
                showWhen = true
                usesChronometer = true
            }

            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val name = applicationContext.getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_HIGH
                NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                    enableLights(false)
                    enableVibration(false)
                    notificationManager.createNotificationChannel(this)
                }
            }

            val notification = NotificationCompat.Builder(applicationContext,
                NOTIFICATION_CHANNEL
            )
                .setSmallIcon(R.drawable.ic_book_play)
                .setContentTitle(bookName)
                .setContentText(trackTitle)

                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(notifyWhen)
                .setShowWhen(showWhen)
                .setUsesChronometer(usesChronometer)
                .setContentIntent(getContentIntent(applicationContext))
                .setOngoing(ongoing)
                .setChannelId(NOTIFICATION_CHANNEL)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setCustomContentView(collapsedView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

            service.startForeground(NOTIFY_ID, notification.build())

            // delay foreground state updating a bit, so the notification can be swiped away properly after initial display
//            Handler(Looper.getMainLooper()).postDelayed({
//                if (!audioServiceBinder.isPlaying()) {
//                    service.stopForeground(false)
//                }
//            }, 200L)
        }

        private fun getContentIntent(applicationContext: Context): PendingIntent {
            val contentIntent = applicationContext.packageManager.getLaunchIntentForPackage("com.allsoftdroid.audiobook")
            contentIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return PendingIntent.getActivity(applicationContext, 0, contentIntent, 0)
        }
    }
}