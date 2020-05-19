package com.allsoftdroid.audiobook.feature_mybooks.di

import com.allsoftdroid.audiobook.feature_mybooks.data.repository.BookMetadataRepositoryImpl
import com.allsoftdroid.audiobook.feature_mybooks.data.repository.LocalBooksRepositoryImpl
import com.allsoftdroid.audiobook.feature_mybooks.domain.IBookMetadataRepository
import com.allsoftdroid.audiobook.feature_mybooks.domain.ILocalBooksRepository
import com.allsoftdroid.audiobook.feature_mybooks.domain.LocalBookListUsecase
import com.allsoftdroid.audiobook.feature_mybooks.presentation.LocalBooksViewModel
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
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
                dataModule,
                jobModule
            )
        )
    }

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            localBooksViewModel,
            usecaseModule,
            dataModule,
            jobModule
        ))
    }

    var localBooksViewModel : Module = module {
        viewModel {
            LocalBooksViewModel(
                bookListUsecase = get()
            )
        }
    }

    var usecaseModule : Module = module {
        factory {
            LocalBookListUsecase(
                localBooksRepository = get(),
                bookMetadataRepository = get()
            )
        }
    }

    var dataModule : Module = module {
        factory {
            LocalBooksRepositoryImpl(
                application = get()
            ) as ILocalBooksRepository
        }

        factory {
            BookMetadataRepositoryImpl(
                metadataDao = get(named(name = BEAN_NAME))
            ) as IBookMetadataRepository
        }

        single(named(name = BEAN_NAME)) {
            AudioBookDatabase.getDatabase(get()).metadataDao() as MetadataDao
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
    private const val BEAN_NAME = "LocalBooksFragment"
}