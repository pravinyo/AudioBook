package com.allsoftdroid.audiobook.services.audio.broadcastReceivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.allsoftdroid.audiobook.services.audio.service.AudioService
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class NotificationPlayerEventBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val eventStore : AudioPlayerEventStore by inject()

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

                        Timber.d("Action Next")
                    }

                    AudioService.PREVIOUS -> {
                        eventStore.publish(Event(Previous(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PREVIOUS_ITEM_INDEX,0),
                            action_need = true
                        ))))
                        Timber.d("Action Previous")
                    }

                    AudioService.PLAY -> {
                        eventStore.publish(Event(Play(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PLAY_ITEM_INDEX,0),
                            action_need = true
                        ))))

                        Timber.d("Action Play")
                    }

                    AudioService.PAUSE -> {
                        eventStore.publish(Event(Pause(PlayingState(
                            playingItemIndex = intent.getIntExtra(ACTION_PAUSE_ITEM_INDEX,0),
                            action_need = true
                        ))))

                        Timber.d("Action Pause")
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