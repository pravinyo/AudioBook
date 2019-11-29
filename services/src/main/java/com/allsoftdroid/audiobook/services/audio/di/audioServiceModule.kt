package com.allsoftdroid.audiobook.services.audio.di

import android.content.Intent
import com.allsoftdroid.audiobook.services.audio.service.AudioService
import com.allsoftdroid.audiobook.services.audio.service.AudioServiceBinder
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(
        intentModule,
        serviceBinderModule
    ))
}


//It's already defined in the app module

//val audioPlayerEventBusModule : Module = module {
//    single {
//        AudioPlayerEventBus.getEventBusInstance()
//    }
//}

val intentModule:Module = module {
    single {
        Intent(get(), AudioService::class.java)
    }
}

val serviceBinderModule : Module = module {
    single {
        AudioServiceBinder(
            get()
        )
    }
}