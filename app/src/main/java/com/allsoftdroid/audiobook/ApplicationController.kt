package com.allsoftdroid.audiobook

import android.app.Application
import com.allsoftdroid.common.di.BaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import android.os.StrictMode


class ApplicationController : Application(){

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ApplicationController)
        }

        BaseModule.injectFeature()

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        enabledStrictMode()
    }

    private fun enabledStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }
    
    
}