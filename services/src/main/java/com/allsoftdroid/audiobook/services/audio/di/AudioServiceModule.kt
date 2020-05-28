package com.allsoftdroid.audiobook.services.audio.di

import android.content.Intent
import com.allsoftdroid.audiobook.services.audio.service.AudioService
import com.allsoftdroid.audiobook.services.audio.service.AudioServiceBinder
import com.allsoftdroid.audiobook.services.audio.utils.AudioBookPlayer
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import com.allsoftdroid.audiobook.services.audio.utils.PrepareMediaHandler
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
                serviceBinderModule,
                playerModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            intentModule,
            serviceBinderModule,
            playerModule
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

    private val playerModule:Module = module {
        single {
            AudioBookPlayer(context = get(),prepareMediaHandler = get())
        }

        single {
            PrepareMediaHandler(context = get(),localStorageFiles = get())
        }
    }
}