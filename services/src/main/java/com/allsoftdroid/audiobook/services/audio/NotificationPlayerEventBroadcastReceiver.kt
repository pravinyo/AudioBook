package com.allsoftdroid.audiobook.services.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.*

class NotificationPlayerEventBroadcastReceiver : BroadcastReceiver(){

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventBus.getEventBusInstance()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {ctx ->
            intent?.let {
                intent ->

                //TODO: sometime it is playing and sometime it is not.
                //TODO: On clicking on tracks it plays as expected
                when(intent.action){
                    AudioService.NEXT -> {
                        eventStore.publish(Event(Next("")))
                        Toast.makeText(ctx,AudioService.NEXT,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PREVIOUS -> {
                        eventStore.publish(Event(Previous("")))
                        Toast.makeText(ctx,AudioService.PREVIOUS,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PLAY -> {
                        eventStore.publish(Event(Play("")))
                        Toast.makeText(ctx,AudioService.PLAY,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PAUSE -> {
                        eventStore.publish(Event(Pause("")))
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