package com.allsoftdroid.feature.book_details.di

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BestBookDetailsParser
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.FetchAdditionalBookDetailsRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.NetworkCachingStoreRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.SearchBookDetailsRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.FetchAdditionalBookDetailsUsecase
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.SearchBookDetailsUsecase
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataApi
import com.allsoftdroid.feature.book_details.data.repository.ListenLaterRepositoryImpl
import com.allsoftdroid.feature.book_details.data.repository.MetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.data.repository.TrackListRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetDownloadUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.ListenLaterUsecase
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


object BookDetailsModule {
    fun injectFeature() = loadFeature

    fun unloadModule() = unloadKoinModules(listOf(
        bookDetailsViewModelModule,
        usecaseModule,
        repositoryModule,
        dataModule,
        networkModule
    ))

    private val loadFeature by lazy {
        loadKoinModules(listOf(
            bookDetailsViewModelModule,
            usecaseModule,
            repositoryModule,
            dataModule,
            networkModule
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
                getMetadataUsecase = get(),
                downloadUsecase = get(),
                searchBookDetailsUsecase = get(),
                listenLaterUsecase = get(),
                getFetchAdditionalBookDetailsUseCase = get(),
                userActionEventStore = get(),
                localFilesForBook = get()
            )
        }
    }

    private val usecaseModule : Module = module {
        factory {
            GetMetadataUsecase(metadataRepository = get())
        }

        factory {
            GetTrackListUsecase(listRepository = get())
        }

        factory {
            GetDownloadUsecase(downloadEventStore = get())
        }

        factory {
            SearchBookDetailsUsecase(
                searchBookDetailsRepository = get()
            )
        }

        factory {
            FetchAdditionalBookDetailsUsecase(
                fetchAdditionBookDetailsRepository = get()
            )
        }

        factory {
            ListenLaterUsecase(
                listenLaterRepository = get()
            )
        }
    }

    var repositoryModule : Module = module {

        factory {
            MetadataRepositoryImpl(
                metadataDao = get(named(name = METADATA_DAO)),
                bookId = getProperty(PROPERTY_BOOK_ID),
                metadataDataSource = get(),
                saveInDatabase = get(named(name = METADATA_DATABASE))) as IMetadataRepository
        }

        factory {
            TrackListRepositoryImpl(
                metadataDao = get(named(name = METADATA_DAO)),
                bookId = getProperty(PROPERTY_BOOK_ID)
            ) as ITrackListRepository
        }

        factory {
            SearchBookDetailsRepositoryImpl(storeCachingRepository = get(),
                bestMatcher = get()) as ISearchBookDetailsRepository
        }

        factory {
            FetchAdditionalBookDetailsRepositoryImpl(storeCachingRepository = get(),
                bookDetailsParser = get()) as IFetchAdditionBookDetailsRepository
        }

        factory {
            ListenLaterRepositoryImpl(listenLaterDao = get()) as IListenLaterRepository
        }
    } @VisibleForTesting set

    private val networkModule : Module = module{
        single{
            NetworkCachingStoreRepositoryImpl(
                networkService = LibriVoxApi.retrofitService,
                bookDetailsService = LibriVoxApi.bookDetailsApiService,
                networkCacheDao = get()) as IStoreRepository
        }
    }

    var dataModule : Module = module {

        single {
            AudioBookDatabase.getDatabase(get()).listenLaterDao()
        }

        single(named(name = METADATA_DAO)) {
            AudioBookDatabase.getDatabase(get()).metadataDao()
        }

        single {
            AudioBookDatabase.getDatabase(get()).networkDao()
        }

        single(named(name = METADATA_DATABASE)) {
            SaveMetadataInDatabase.setup(metadataDao = get(named(name = METADATA_DAO))) as SaveInDatabase<MetadataDao,SaveMetadataInDatabase>
        }

        single {
            ArchiveMetadataApi.RETROFIT_SERVICE
        }
        
        single{
            BestBookDetailsParser()
        }

        single {
            BookDetailsParserFromHtml()
        }
    } @VisibleForTesting set

    const val PROPERTY_BOOK_ID = "bookDetails_book_id"
    private const val METADATA_DATABASE = "SaveMetadataInDatabase"
    private const val METADATA_DAO ="MetadataDao_BookDetailsModule"
}