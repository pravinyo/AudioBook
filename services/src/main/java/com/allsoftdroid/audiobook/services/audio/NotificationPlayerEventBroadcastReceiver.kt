package com.allsoftdroid.audiobook.services.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.*

class NotificationPlayerEventBroadcastReceiver : BroadcastReceiver(){

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventBus.getEventBusInstance()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {ctx ->
            intent?.let {
                intent ->

                when(intent.action){
                    AudioService.NEXT -> {

                        eventStore.publish(Event(Next(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_NEXT_ITEM_INDEX,0),
                            action_need = true
                        ))))

                        Toast.makeText(ctx,AudioService.NEXT,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PREVIOUS -> {
                        eventStore.publish(Event(Previous(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PREVIOUS_ITEM_INDEX,0),
                            action_need = true
                        ))))
                        Toast.makeText(ctx,AudioService.PREVIOUS,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PLAY -> {
                        eventStore.publish(Event(Play(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PLAY_ITEM_INDEX,0),
                            action_need = true
                        ))))

                        Toast.makeText(ctx,AudioService.PLAY,Toast.LENGTH_SHORT).show()
                    }

                    AudioService.PAUSE -> {
                        eventStore.publish(Event(Pause(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PAUSE_ITEM_INDEX,0),
                            action_need = true
                        ))))

                        Toast.makeText(ctx,AudioService.PAUSE,Toast.LENGTH_SHORT).show()
                    }

                    else -> Toast.makeText(ctx,"Unknown Intent action",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        fun newPendingIntent(context: Context,action : String,requestCode:Int,key:String,value:Int): PendingIntent {
            val intent = Intent(context, NotificationPlayerEventBroadcastReceiver::class.java)
            intent.action = action
            intent.putExtra(key,value)
            return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        const val ACTION_NEXT_ITEM_INDEX = "action_next_index"
        const val ACTION_PREVIOUS_ITEM_INDEX = "action_previous_index"
        const val ACTION_PLAY_ITEM_INDEX = "action_play_index"
        const val ACTION_PAUSE_ITEM_INDEX = "action_pause_index"
    }
}