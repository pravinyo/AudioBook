package com.allsoftdroid.audiobook.feature_listen_later_ui.utils

import android.app.Activity
import android.content.Intent
import com.allsoftdroid.audiobook.feature_listen_later_ui.R

object ShareUtils {

    fun share(context: Activity, subject:String, txt:String){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,subject)
        shareIntent.putExtra(Intent.EXTRA_TITLE,subject)
        shareIntent.putExtra(Intent.EXTRA_TEXT,txt)
        shareIntent.type = "text/plain"

        context.startActivity(Intent.createChooser(shareIntent,context.getString(R.string.share_using)))
    }
}