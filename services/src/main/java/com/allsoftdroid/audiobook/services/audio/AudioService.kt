package com.allsoftdroid.audiobook.services.audio

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.services.AudioServiceBinder


class AudioService : Service(){

    private val audioServiceBinder by lazy { AudioServiceBinder() }

    companion object{
        //notification id
        private const val NOTIFY_ID = 1
        private const val NOTIFICATION_CHANNEL = "audio_book_music_player_channel"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return audioServiceBinder
    }

    override fun onCreate() {
        super.onCreate()
        audioServiceBinder.trackTitle.observeForever {
            it?.let {
                setupNotification(title = it)
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        audioServiceBinder.stopAudio()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    @SuppressLint("NewApi")
    private fun setupNotification(title:String) {

        val playPauseIcon = if (audioServiceBinder.isPlaying()) R.drawable.play_circle_outline else R.drawable.play_circle

        var notifWhen = 0L
        var showWhen = false
        var usesChronometer = false
        var ongoing = false
        if (audioServiceBinder.isPlaying()) {
            notifWhen = System.currentTimeMillis() - (audioServiceBinder.getCurrentAudioPosition())
            showWhen = true
            usesChronometer = true
            ongoing = true
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = resources.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText("artist")
            .setSmallIcon(R.drawable.ic_book_play)
//            .setLargeIcon(mCurrSongCover)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(notifWhen)
            .setShowWhen(showWhen)
            .setUsesChronometer(usesChronometer)
//            .setContentIntent(getContentIntent())
            .setOngoing(ongoing)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setCategory(Notification.CATEGORY_SERVICE)
//            .addAction(R.drawable.play_circle, getString(R.string.previous), getIntent("PREV"))
//            .addAction(playPauseIcon, getString(R.string.playpause), getIntent("PP"))
//            .addAction(R.drawable.play_circle_outline, getString(R.string.next), getIntent("NEX"))

        startForeground(NOTIFY_ID, notification.build())

        // delay foreground state updating a bit, so the notification can be swiped away properly after initial display
        Handler(Looper.getMainLooper()).postDelayed({
            if (!audioServiceBinder.isPlaying()) {
                stopForeground(false)
            }
        }, 200L)

    }

//    private fun getContentIntent(): PendingIntent {
//        val contentIntent = Intent(this,com.allsoftdroid.audiobook.MainActivity::class.java)
//        return PendingIntent.getActivity(this, 0, contentIntent, 0)
//    }
//
//    private fun getIntent(action: String): PendingIntent {
//        val intent = Intent(this, com.allsoftdroid.audiobook.MainActivity::class.java)
//        intent.action = action
//        return PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
//    }
}