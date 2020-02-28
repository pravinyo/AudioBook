package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.di

import androidx.annotation.VisibleForTesting
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.MainPlayerViewModel
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
                jobModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            mainPlayerViewModelModule,
            jobModule
        ))
    }

    var mainPlayerViewModelModule : Module = module {
        viewModel{
            MainPlayerViewModel(eventStore = get())
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


    const val SUPER_VISOR_JOB = "MainPlayerSuperVisorJob"
    const val VIEW_MODEL_SCOPE = "MainPlayerViewModelScope"
}