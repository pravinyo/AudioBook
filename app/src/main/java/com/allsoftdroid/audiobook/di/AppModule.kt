package com.allsoftdroid.audiobook.di

import android.content.Context
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.store.AudioPlayerEventBus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


object AppModule {
    fun injectFeature() = loadFeature

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            viewModelModule,
            audioManagerModule,
            audioPlayerEventBusModule,
            connectivityModule
        ))
    }

    private val viewModelModule: Module = module{
        viewModel {
            MainActivityViewModel(get())
        }
    }

    private val audioPlayerEventBusModule : Module = module {
        single {
            AudioPlayerEventBus.getEventBusInstance()
        }
    }


    private val audioManagerModule : Module = module {
        single {
            AudioManager.getInstance(get())
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