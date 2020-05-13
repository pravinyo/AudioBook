package com.allsoftdroid.audiobook.feature_listen_later_ui.di

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.repository.ListenLaterRepositoryImpl
import com.allsoftdroid.audiobook.feature_listen_later_ui.domain.IListenLaterRepository
import com.allsoftdroid.audiobook.feature_listen_later_ui.presentation.ListenLaterViewModel
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
            ListenLaterViewModel(repository = get())
        }
    }

    var dataModule : Module = module {
        single(named(name = BEAN_NAME)) {
            AudioBookDatabase.getDatabase(get()).listenLaterDao()
        }

        factory {
            ListenLaterRepositoryImpl(listenLaterDao = get(named(name = BEAN_NAME))) as IListenLaterRepository
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

    const val SUPER_VISOR_JOB = "SuperVisorJob_ListenLater"
    const val VIEW_MODEL_SCOPE = "ViewModelScope_ListenLater"
    const val BEAN_NAME = "ListenLaterFragment"
}