package com.allsoftdroid.audiobook.services.audio.utils

import android.app.Application
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class PrepareMediaHandler(private val context:Application,
                          private val localStorageFiles: LocalFilesForBook) {

    fun createMediaSource(bookId:String,playlist : List<AudioPlayListItem>): ConcatenatingMediaSource {
        Timber.d("Create media source called")
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(
            R.string.audio_player_service)))
        val concatenatingMediaSource = ConcatenatingMediaSource()

        Timber.d("Building media list")
        val files = localStorageFiles.getListHavingOnlineAndOfflineUrl(bookId,playlist)

        for (file in files) {
            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(file)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }
}