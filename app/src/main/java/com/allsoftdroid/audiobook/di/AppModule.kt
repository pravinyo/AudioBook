package com.allsoftdroid.audiobook.di

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.allsoftdroid.audiobook.domain.usecase.GetLastPlayedUsecase
import com.allsoftdroid.audiobook.feature_downloader.data.Downloader2
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloaderCore
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.services.audio.AudioManager
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


object AppModule {
    fun injectFeature() = loadFeature

    fun unloadModule() = unloadKoinModules(
        listOf(
            dataModule,
            viewModelModule,
            audioManagerModule,
            downloaderModule,
            usecaseModule
    ))

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            dataModule,
            viewModelModule,
            audioManagerModule,
            downloaderModule,
            usecaseModule
        ))
    }

    private val viewModelModule: Module = module{
        viewModel {
            (state: SavedStateHandle) ->
            MainActivityViewModel(
                stateHandle = state,
                application = get(),
                sharedPref = get(),
                audioManager = get(),
                eventStore = get(),
                handler = get(),
                lastPlayedUsecase = get()
            )
        }
    }

    private val downloaderModule : Module = module {
        single {
                (ctx:Activity) ->
            Downloader2(
                get(),
                get()
            ) as IDownloaderCore
        }
    }

    private val audioManagerModule : Module = module {
        single {
            AudioManager.getInstance(get())
        }
    }

    private val usecaseModule : Module = module {
        factory {
            UseCaseHandler.getInstance()
        }

        factory {
            GetLastPlayedUsecase()
        }
    }

    private val dataModule :Module = module {
        single<BookDetailsSharedPreferenceRepository> {
            BookDetailsSharedPreferencesRepositoryImpl.create(context = get())
        }

        single {
            LocalFilesForBook(app = get())
        }
    }
}