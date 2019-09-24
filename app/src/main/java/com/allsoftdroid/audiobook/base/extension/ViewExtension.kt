package com.allsoftdroid.audiobook.base.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).show()
}