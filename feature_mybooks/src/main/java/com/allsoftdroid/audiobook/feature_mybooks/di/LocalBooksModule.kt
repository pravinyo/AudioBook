package com.allsoftdroid.audiobook.feature_mybooks.di

import com.allsoftdroid.audiobook.feature_mybooks.presentation.LocalBooksViewModel
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

object LocalBooksModule {

    fun injectFeature() =
        loadFeature

    fun unLoadModules(){
        unloadKoinModules(
            listOf(
                localBooksViewModel,
                jobModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            localBooksViewModel,
            jobModule
        ))
    }

    var localBooksViewModel : Module = module {
        viewModel {
            LocalBooksViewModel()
        }
    }

    var jobModule : Module = module {

        single(named(name = SUPER_VISOR_JOB)) {
            SupervisorJob()
        }

        factory(named(name = VIEW_MODEL_SCOPE)) {
            CoroutineScope(get(named(name = SUPER_VISOR_JOB)) as CoroutineContext + Dispatchers.Main)
        }
    }

    const val SUPER_VISOR_JOB = "SuperVisorJob_LocalBooks"
    const val VIEW_MODEL_SCOPE = "ViewModelScope_LocalBooks"
    const val BEAN_NAME = "LocalBooksFragment"
}