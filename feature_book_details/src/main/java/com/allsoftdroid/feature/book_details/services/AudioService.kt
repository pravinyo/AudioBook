package com.allsoftdroid.feature.book_details.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AudioService : Service(){

    private val audioServiceBinder = AudioServiceBinder()
    override fun onBind(p0: Intent?): IBinder? {
        return audioServiceBinder
    }
}