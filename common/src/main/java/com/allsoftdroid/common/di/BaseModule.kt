package com.allsoftdroid.common.di

import android.content.Context
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.audioPlayer.AudioPlayerEventBus
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import com.allsoftdroid.common.base.store.userAction.UserActionEventBus
import com.allsoftdroid.common.base.store.userAction.UserActionEventStore
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object BaseModule {
    fun injectFeature() = loadFeature

    fun unloadModule() = unloadKoinModules(
        listOf(
            audioPlayerEventBusModule,
            downloaderEventBusModule,
            userActionEventBusModule,
            connectivityModule
        )
    )

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            audioPlayerEventBusModule,
            downloaderEventBusModule,
            userActionEventBusModule,
            connectivityModule
        ))
    }

    private val audioPlayerEventBusModule : Module = module {
        single {
            AudioPlayerEventBus.getEventBusInstance()
        }
    }

    private val downloaderEventBusModule : Module = module {
        single {
            DownloaderEventBus.getEventBusInstance()
        }
    }

    private val userActionEventBusModule : Module = module {
        single {
            UserActionEventBus.getEventBusInstance()
        }
    }

    private val connectivityModule : Module = module {
        factory {(context: Context) ->
            ConnectionLiveData(
                context
            )
        }
    }
}