package com.allsoftdroid.library_base.presentation.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

    @get:LayoutRes
    protected abstract val layoutResId:Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutResId)

        supportActionBar?.hide()

        Timber.v("onCreate ${javaClass.simpleName}")
    }
}