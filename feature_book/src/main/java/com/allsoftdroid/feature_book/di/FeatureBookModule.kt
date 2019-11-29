package com.allsoftdroid.feature_book.di

import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature_book.data.repository.AudioBookRepositoryImpl
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import com.allsoftdroid.feature_book.domain.usecase.GetAudioBookListUsecase
import com.allsoftdroid.feature_book.presentation.viewModel.AudioBookListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext


fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(
        bookListViewModelModule,
        usecaseModule,
        repositoryModule,
        databaseModule,
        jobModule
    ))
}

val bookListViewModelModule : Module = module {
    viewModel {
        AudioBookListViewModel(
            application = get(),
            useCaseHandler = get(),
            getAlbumListUseCase = get()
        )
    }
}

val usecaseModule : Module = module {
    factory {
        UseCaseHandler.getInstance()
    }

    factory {
        GetAudioBookListUsecase(audioBookRep = get())
    }
}

val repositoryModule : Module = module {
    single {
        AudioBookRepositoryImpl(get()) as AudioBookRepository
    }
}


val databaseModule : Module = module {
    single {
        AudioBookDatabase.getDatabase(get()).audioBooksDao()
    }

    single {
        AudioBookDatabase.getDatabase(get()).metadataDao()
    }
}

val jobModule : Module = module {

    single(named(name = SUPER_VISOR_JOB)) {
        SupervisorJob()
    }

    factory(named(name = VIEW_MODEL_SCOPE)) {
        CoroutineScope(get(named(name = SUPER_VISOR_JOB)) as CoroutineContext+ Dispatchers.Main)
    }
}

const val SUPER_VISOR_JOB = "SuperVisorJob"
const val VIEW_MODEL_SCOPE = "ViewModelScope"