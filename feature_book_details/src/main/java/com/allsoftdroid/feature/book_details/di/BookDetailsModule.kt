package com.allsoftdroid.feature.book_details.di

import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.feature.book_details.data.repository.AudioBookMetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


object BookDetailsModule {
    fun injectFeature() = loadFeature

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            bookDetailsViewModelModule,
            usecaseModule,
            repositoryModule,
            dataModule
        ))
    }

    private val bookDetailsViewModelModule : Module = module {

        viewModel {
            BookDetailsViewModel(
                application = get(),
                useCaseHandler = get(),
                getTrackListUsecase = get(),
                getMetadataUsecase = get()
            )
        }
    }

    private val usecaseModule : Module = module {
        factory {
            GetMetadataUsecase(metadataRepository = get())
        }

        factory {
            GetTrackListUsecase(metadataRepository = get())
        }
    }

    private val repositoryModule : Module = module {

        factory {
            AudioBookMetadataRepositoryImpl(metadataDao = get(),bookId = getProperty(PROPERTY_BOOK_ID)) as AudioBookMetadataRepository
        }
    }

    private val dataModule : Module = module {
        single {
            AudioBookDatabase.getDatabase(get()).metadataDao()
        }
    }

    const val PROPERTY_BOOK_ID = "bookDetails_book_id"
}