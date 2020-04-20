package com.allsoftdroid.audiobook.services.audio.di

import android.content.Intent
import com.allsoftdroid.audiobook.services.audio.service.AudioService
import com.allsoftdroid.audiobook.services.audio.service.AudioServiceBinder
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object AudioServiceModule {
    fun injectFeature() = loadFeature

    fun unLoadModules(){
        unloadKoinModules(
            listOf(
                intentModule,
                serviceBinderModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            intentModule,
            serviceBinderModule
        ))
    }

    private val intentModule:Module = module {
        single {
            Intent(get(), AudioService::class.java)
        }
    }

    private val serviceBinderModule : Module = module {
        single {
            AudioServiceBinder(
                get()
            )
        }
    }
}