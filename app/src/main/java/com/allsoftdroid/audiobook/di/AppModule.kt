package com.allsoftdroid.audiobook.di

import android.content.Context
import com.allsoftdroid.audiobook.feature_downloader.data.Downloader
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.audioPlayer.AudioPlayerEventBus
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


object AppModule {
    fun injectFeature() = loadFeature

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            dataModule,
            viewModelModule,
            audioManagerModule,
            audioPlayerEventBusModule,
            downloaderModule,
            connectivityModule,
            usecaseModule
        ))
    }

    private val viewModelModule: Module = module{
        viewModel {
            MainActivityViewModel(
                application = get(),
                sharedPref = get(),
                audioManager = get(),
                eventStore = get()
            )
        }
    }

    private val audioPlayerEventBusModule : Module = module {
        single {
            AudioPlayerEventBus.getEventBusInstance()
        }
    }

    private val downloaderModule : Module = module {
        single {
            DownloaderEventBus.getEventBusInstance()
        }

        single {
                (ctx:Context) ->
            Downloader(
                ctx,
                get()
            )
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

    private val usecaseModule : Module = module {
        factory {
            UseCaseHandler.getInstance()
        }
    }

    private val dataModule :Module = module {
        single<BookDetailsSharedPreferenceRepository> {
            BookDetailsSharedPreferencesRepositoryImpl.create(context = get())
        }
    }
}