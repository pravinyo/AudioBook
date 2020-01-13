package com.allsoftdroid.feature.book_details.di

import androidx.lifecycle.SavedStateHandle
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataApi
import com.allsoftdroid.feature.book_details.data.repository.AudioBookMetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
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
            (handle: SavedStateHandle) ->
            BookDetailsViewModel(
                sharedPref = get(),
                stateHandle = handle,
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
            AudioBookMetadataRepositoryImpl(
                metadataDao = get(),
                bookId = getProperty(PROPERTY_BOOK_ID),
                metadataDataSource = get(),
                saveInDatabase = get(named(name = METADATA_DATABASE))) as AudioBookMetadataRepository
        }
    }

    private val dataModule : Module = module {
        single {
            AudioBookDatabase.getDatabase(get()).metadataDao()
        }

        single(named(name = METADATA_DATABASE)) {
            SaveMetadataInDatabase.setup(metadataDao = get()) as SaveInDatabase<MetadataDao,SaveMetadataInDatabase>
        }

        single {
            ArchiveMetadataApi.RETROFIT_SERVICE
        }
    }

    const val PROPERTY_BOOK_ID = "bookDetails_book_id"
    private const val METADATA_DATABASE = "SaveMetadataInDatabase"
}