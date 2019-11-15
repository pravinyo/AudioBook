package com.allsoftdroid.audiobook.services.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationPlayerEventBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {ctx ->
            intent?.let {
                intent ->

                when(intent.action){
                    AudioService.NEXT -> {
                        Toast.makeText(ctx,AudioService.NEXT,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PREVIOUS -> {
                        Toast.makeText(ctx,AudioService.PREVIOUS,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PLAY -> {
                        Toast.makeText(ctx,AudioService.PLAY,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PAUSE -> {
                        Toast.makeText(ctx,AudioService.PAUSE,Toast.LENGTH_SHORT).show()
                    }

                    else -> Toast.makeText(ctx,"Unknown Intent action",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        fun newPendingIntent(context: Context,action : String,requestCode:Int): PendingIntent {
            val intent = Intent(context, NotificationPlayerEventBroadcastReceiver::class.java)
            intent.action = action
            return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}