package com.allsoftdroid.audiobook.feature_downloader.domain.broadcastReceiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.store.downloader.PullAndUpdateStatus
import com.allsoftdroid.common.base.store.userAction.OpenDownloadUI
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class DownloadManagerBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val downloadEventStore : DownloadEventStore by inject()
    private val userActionEventStore : UserActionEventStore by inject()

    override fun onReceive(context: Context?, intent: Intent?) {

        context?.let {
            intent?.let {

                when(intent.action){
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                        Timber.d("Downloads Completed")
                        if(intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)){
                            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                            if(downloadId > 0){
                                downloadEventStore.publish(
                                    Event(PullAndUpdateStatus(downloadId = downloadId))
                                )
                            }
                        }
                    }

                    DownloadManager.ACTION_VIEW_DOWNLOADS,
                    DownloadManager.ACTION_NOTIFICATION_CLICKED ->{
                        userActionEventStore.publish(
                            Event(OpenDownloadUI(this::class.java.simpleName))
                        )
                    }
                }

            }
        }
    }
}