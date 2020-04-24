package com.allsoftdroid.audiobook.services.audio.utils

import android.app.Application
import androidx.core.net.toUri
import com.allsoftdroid.audiobook.services.R
import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class PrepareMediaHandler(private val context:Application) {

    fun createMediaSource(bookId:String,playlist : List<AudioPlayListItem>): ConcatenatingMediaSource {
        Timber.d("Create media source called")
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(
            R.string.audio_player_service)))
        val concatenatingMediaSource = ConcatenatingMediaSource()

        Timber.d("Building media list")
        for (sample in playlist) {
            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(
                ArchiveUtils.getRemoteFilePath(sample.filename,bookId).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }
}