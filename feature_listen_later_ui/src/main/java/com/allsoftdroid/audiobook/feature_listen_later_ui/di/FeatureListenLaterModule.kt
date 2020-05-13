package com.allsoftdroid.audiobook.feature_listen_later_ui.di

import androidx.annotation.VisibleForTesting
import com.allsoftdroid.audiobook.feature_listen_later_ui.ListenLaterViewModel
import com.allsoftdroid.database.common.AudioBookDatabase
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

object FeatureListenLaterModule {

    fun injectFeature() =
        loadFeature

    fun unLoadModules(){
        unloadKoinModules(
            listOf(
                listenLaterViewModel,
                dataModule,
                jobModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            listenLaterViewModel,
            dataModule,
            jobModule
        ))
    }

    var listenLaterViewModel : Module = module {
        viewModel {
            ListenLaterViewModel()
        }
    }
        @VisibleForTesting set

    var dataModule : Module = module {
        single {
            AudioBookDatabase.getDatabase(get()).listenLaterDao()
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

    const val SUPER_VISOR_JOB = "SuperVisorJob"
    const val VIEW_MODEL_SCOPE = "ViewModelScope"
}