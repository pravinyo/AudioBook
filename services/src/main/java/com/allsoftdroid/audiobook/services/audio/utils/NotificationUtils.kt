package com.allsoftdroid.audiobook.services.audio.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.audiobook.services.audio.service.AudioService
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.utils.ImageUtils
import com.allsoftdroid.audiobook.services.audio.broadcastReceivers.NotificationPlayerEventBroadcastReceiver.Companion as NotificationPlayerEventBroadcastReceiver1


class NotificationUtils {

    companion object{

        //notification id
        private const val NOTIFY_ID = 1
        private const val NOTIFICATION_CHANNEL = "audio_book_music_player_channel"

        fun sendNotification(isAudioPlaying:Boolean, currentAudioPos:Int, service: AudioService, applicationContext: Context, trackTitle:String, bookName:String) {
            try{
                val collapsedView = RemoteViews(applicationContext.packageName, R.layout.notification_mini_player_collapsed)
                val normalView = RemoteViews(applicationContext.packageName, R.layout.notification_mini_player)

                var playPauseIcon = 0

                when (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        playPauseIcon = if (!isAudioPlaying) R.drawable.ic_play_arrow_black_24dp else R.drawable.ic_pause_black_24dp
                        collapsedView.setImageViewResource(R.id.image_notification_prev,R.drawable.ic_skip_previous_black_24dp)
                        collapsedView.setImageViewResource(R.id.image_notification_next,R.drawable.ic_skip_next_black_24dp)

                        collapsedView.setTextColor(R.id.notification_track_name,getColorId(applicationContext,R.color.black))
                        collapsedView.setTextColor(R.id.notification_book_name,getColorId(applicationContext,R.color.black))

                        normalView.setTextColor(R.id.notification_track_name,getColorId(applicationContext,R.color.black))
                    } // Night mode is not active, we're using the light theme

                    Configuration.UI_MODE_NIGHT_YES -> {

                        playPauseIcon = if (!isAudioPlaying) R.drawable.ic_play_arrow_white_24dp else R.drawable.ic_pause_white_24dp

                        collapsedView.setImageViewResource(R.id.image_notification_prev,R.drawable.ic_skip_previous_white_24dp)
                        collapsedView.setImageViewResource(R.id.image_notification_next,R.drawable.ic_skip_next_white_24dp)

                        collapsedView.setTextColor(R.id.notification_track_name,getColorId(applicationContext,R.color.white))
                        collapsedView.setTextColor(R.id.notification_book_name,getColorId(applicationContext,R.color.white))

                        normalView.setTextColor(R.id.notification_track_name,getColorId(applicationContext,R.color.white))
                    } // Night mode is active, we're using dark theme
                }

                collapsedView.setTextViewText(R.id.notification_book_name,TextFormatter.getPartialString(bookName))

                collapsedView.setTextViewText(R.id.notification_track_name,TextFormatter.getPartialString(trackTitle))
                normalView.setTextViewText(R.id.notification_track_name,TextFormatter.getPartialString(trackTitle))

                collapsedView.setImageViewResource(R.id.image_notification_playpause,playPauseIcon)
                normalView.setImageViewResource(R.id.image_notification_playpause,playPauseIcon)

                val drawable = CreateImageOverlay
                    .with(applicationContext)
                    .buildOverlay(front = R.mipmap.ic_launcher,back = R.drawable.gradiant_background)

                val roundImage  = ImageUtils.getCircleBitmap(drawable.toBitmap())

                collapsedView.setImageViewBitmap(R.id.notification_track_thumbnail,roundImage)
                normalView.setImageViewBitmap(R.id.notification_track_thumbnail,roundImage)

                collapsedView.setOnClickPendingIntent(
                    R.id.image_notification_prev,
                    NotificationPlayerEventBroadcastReceiver1.newPendingIntent(
                        context = applicationContext,
                        action = AudioService.PREVIOUS,
                        requestCode = AudioService.ACTION_PREVIOUS,
                        key = NotificationPlayerEventBroadcastReceiver1.ACTION_PREVIOUS_ITEM_INDEX,
                        value = if (currentAudioPos>1) currentAudioPos-1 else 0))

                collapsedView.setOnClickPendingIntent(R.id.image_notification_next,
                    NotificationPlayerEventBroadcastReceiver1.newPendingIntent(
                        context = applicationContext,
                        action = AudioService.NEXT,
                        requestCode = AudioService.ACTION_NEXT,
                        key = NotificationPlayerEventBroadcastReceiver1.ACTION_NEXT_ITEM_INDEX,
                        value = currentAudioPos+1
                    ))

                collapsedView.setOnClickPendingIntent(R.id.image_notification_playpause,
                    NotificationPlayerEventBroadcastReceiver1.newPendingIntent(
                        context = applicationContext,
                        action = if(isAudioPlaying) AudioService.PAUSE else AudioService.PLAY,
                        requestCode = AudioService.ACTION_PLAY_PAUSE,
                        key = if(isAudioPlaying) NotificationPlayerEventBroadcastReceiver1.ACTION_PAUSE_ITEM_INDEX else
                            NotificationPlayerEventBroadcastReceiver1.ACTION_PLAY_ITEM_INDEX,
                        value = currentAudioPos
                    ))

                normalView.setOnClickPendingIntent(R.id.image_notification_playpause,
                    NotificationPlayerEventBroadcastReceiver1.newPendingIntent(
                        context = applicationContext,
                        action = if(isAudioPlaying) AudioService.PAUSE else AudioService.PLAY,
                        requestCode = AudioService.ACTION_PLAY_PAUSE,
                        key = if(isAudioPlaying) NotificationPlayerEventBroadcastReceiver1.ACTION_PAUSE_ITEM_INDEX else
                            NotificationPlayerEventBroadcastReceiver1.ACTION_PLAY_ITEM_INDEX,
                        value = currentAudioPos
                    ))

                var notifyWhen = 0L
                var showWhen = false
                var usesChronometer = false
                var ongoing = false
                if (isAudioPlaying) {
                    notifyWhen = System.currentTimeMillis() - (currentAudioPos)
                    showWhen = true
                    usesChronometer = true
                    ongoing = true
                }

                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val name = applicationContext.getString(R.string.app_name)
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
                    .setContentIntent(
                        getContentIntent(
                            applicationContext
                        )
                    )
                    .setOngoing(ongoing)
                    .setChannelId(NOTIFICATION_CHANNEL)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setCustomContentView(normalView)
                    .setCustomBigContentView(collapsedView)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())

                if(!isAudioPlaying) notification.setAutoCancel(true)
                service.startForeground(NOTIFY_ID, notification.build())
            }catch (e:Exception){
                e.printStackTrace()
            }

             //delay foreground state updating a bit, so the notification can be swiped away properly after initial display
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isAudioPlaying) {
                    service.stopForeground(false)
                }
            }, 200L)
        }

        private fun getColorId(context: Context,@ColorRes color: Int): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getColor(color)
            } else {
                context.resources.getColor(color)
            }
        }

        private fun getContentIntent(applicationContext: Context): PendingIntent {
            val contentIntent = applicationContext.packageManager.getLaunchIntentForPackage("com.allsoftdroid.audiobook")
            contentIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return PendingIntent.getActivity(applicationContext, 0, contentIntent, 0)
        }
    }
}