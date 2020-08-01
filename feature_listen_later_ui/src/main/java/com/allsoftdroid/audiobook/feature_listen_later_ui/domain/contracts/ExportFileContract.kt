package com.allsoftdroid.audiobook.feature_listen_later_ui.domain.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class ExportFileContract: ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, documentName: String): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, documentName)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = when{
        resultCode != Activity.RESULT_OK -> null      // Return null, if action is cancelled
        else -> intent?.data       // Return the data
    }
}