package com.allsoftdroid.audiobook.services.audio

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.AudioPlayerEventStore
import com.allsoftdroid.common.base.store.Initial
import com.allsoftdroid.common.base.store.Next
import com.allsoftdroid.common.base.utils.ImageUtils


class AudioService : Service(){

    companion object CONSTANT{
        const val ACTION_PREVIOUS = 0
        const val ACTION_PLAY_PAUSE=1
        const val ACTION_NEXT = 2

        //notification id
        private const val NOTIFY_ID = 1
        private const val NOTIFICATION_CHANNEL = "audio_book_music_player_channel"
    }

    private val audioServiceBinder by lazy {
        AudioServiceBinder(
            application
        )
    }

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventStore.getInstance(Event(Initial("")))
    }

    override fun onBind(p0: Intent?): IBinder? {
        return audioServiceBinder
    }

    override fun onCreate() {
        super.onCreate()
        audioServiceBinder.trackTitle.observeForever {
            it?.let {
                buildNotification()
            }
        }

        audioServiceBinder.nextTrack.observeForever {
            it.getContentIfNotHandled()?.let {nextEvent ->
                if(nextEvent){
                    eventStore.publish(Event(Next("")))
                }
            }
        }
    }



    private fun buildNotification() {
        sendNotification(
            trackTitle = audioServiceBinder.getCurrentTrackTitle()?:"UNKNOWN",
            bookId = audioServiceBinder.getBookId(),
            bookName = audioServiceBinder.getBookId())
    }

    override fun onUnbind(intent: Intent?): Boolean {
        audioServiceBinder.stopAudio()
        audioServiceBinder.destroyAudioPlayer()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun getContentIntent(): PendingIntent {
        val contentIntent = packageManager.getLaunchIntentForPackage("com.allsoftdroid.audiobook")
        contentIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(this, 0, contentIntent, 0)
    }

//    private fun getIntent(action: String): PendingIntent {
//
//        val intent = packageManager.getLaunchIntentForPackage("com.allsoftdroid.audiobook")
//        intent?.let {
//            it.action = action
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//
//        return PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
//    }

    @SuppressLint("NewApi")
    private fun sendNotification(trackTitle:String,bookId: String,bookName:String) {

        val collapsedView = RemoteViews(applicationContext.packageName,R.layout.notification_mini_player_collapsed)
        val playPauseIcon = if (audioServiceBinder.isPlaying()) R.drawable.ic_play_arrow_black_24dp else R.drawable.ic_pause_black_24dp

        collapsedView.setTextViewText(R.id.notification_book_name,bookName)
        collapsedView.setTextViewText(R.id.notification_track_name,trackTitle)
        collapsedView.setImageViewResource(R.id.image_notification_playpause,playPauseIcon)

        val drawable = CreateImageOverlay
            .with(applicationContext)
            .buildOverlay(front = R.drawable.ic_book_play,back = R.drawable.gradiant_background)

        val roundImage  = ImageUtils.getCircleBitmap(drawable.toBitmap())

        collapsedView.setImageViewBitmap(R.id.notification_track_thumbnail,roundImage)

        val intentPrevious = Intent(applicationContext,AudioService::class.java)
        intentPrevious.action = "Previous"

        val previousPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ACTION_PREVIOUS,
            intentPrevious,
            PendingIntent.FLAG_UPDATE_CURRENT)

        collapsedView.setOnClickPendingIntent(R.id.image_notification_prev,previousPendingIntent)

        val intentNext = Intent(applicationContext,AudioService::class.java)
        intentNext.action = "Next"

        val nextPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ACTION_NEXT,
            intentNext,
            PendingIntent.FLAG_UPDATE_CURRENT)

        collapsedView.setOnClickPendingIntent(R.id.image_notification_next,nextPendingIntent)

        val intentPlayPause = Intent(applicationContext,AudioService::class.java)
        intentPlayPause.action = "PlayPause"

        val playPausePendingIntent = PendingIntent.getActivity(
            applicationContext,
            ACTION_PLAY_PAUSE,
            intentPlayPause,
            PendingIntent.FLAG_UPDATE_CURRENT)

        collapsedView.setOnClickPendingIntent(R.id.image_notification_playpause,playPausePendingIntent)


        var notifyWhen = 0L
        var showWhen = false
        var usesChronometer = false
        val ongoing = true
        if (audioServiceBinder.isPlaying()) {
            notifyWhen = System.currentTimeMillis() - (audioServiceBinder.getCurrentAudioPosition())
            showWhen = true
            usesChronometer = true
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = applicationContext.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
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
            .setContentIntent(getContentIntent())
            .setOngoing(ongoing)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setCustomContentView(collapsedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        startForeground(NOTIFY_ID, notification.build())

        // delay foreground state updating a bit, so the notification can be swiped away properly after initial display
        Handler(Looper.getMainLooper()).postDelayed({
            if (!audioServiceBinder.isPlaying()) {
                stopForeground(false)
            }
        }, 200L)
    }
}