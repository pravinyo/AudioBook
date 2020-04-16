package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di

import androidx.annotation.VisibleForTesting
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.MainPlayerViewModel
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase.GetPlayingTrackProgressUsecase
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.domain.usecase.GetTrackRemainingTimeUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

object FeatureMainPlayerModule {

    fun injectFeature() = loadFeature

    fun unLoadModules(){
        unloadKoinModules(
            listOf(
                mainPlayerViewModelModule,
                jobModule,
                usecaseModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            mainPlayerViewModelModule,
            jobModule,
            usecaseModule
        ))
    }

    var mainPlayerViewModelModule : Module = module {
        viewModel{
            MainPlayerViewModel(eventStore = get(),
                useCaseHandler = get(),
                trackProgressUsecase = get(),
                remainingTimeUsecase = get())
        }
    }
        @VisibleForTesting set

    var jobModule : Module = module {

        single(named(name = SUPER_VISOR_JOB)) {
            SupervisorJob()
        }

        factory(named(name = VIEW_MODEL_SCOPE)) {
            CoroutineScope(get(named(name = SUPER_VISOR_JOB)) as CoroutineContext + Dispatchers.Main)
        }
    }
        @VisibleForTesting set

    var usecaseModule:Module = module {
        factory {
            GetPlayingTrackProgressUsecase(audioManager = get())
        }

        factory {
            GetTrackRemainingTimeUsecase(audioManager = get())
        }
    }

    const val SUPER_VISOR_JOB = "MainPlayerSuperVisorJob"
    const val VIEW_MODEL_SCOPE = "MainPlayerViewModelScope"
}