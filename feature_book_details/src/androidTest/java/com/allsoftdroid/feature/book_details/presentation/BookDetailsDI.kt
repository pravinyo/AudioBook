package com.allsoftdroid.feature.book_details.presentation

import androidx.lifecycle.SavedStateHandle
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.LibriVoxApi
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.FetchAdditionalBookDetailsRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.NetworkCachingStoreRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository.SearchBookDetailsRepositoryImpl
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.FetchAdditionalBookDetailsUsecase
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.usecase.SearchBookDetailsUsecase
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BestBookDetailsParser
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.allsoftdroid.common.base.store.audioPlayer.AudioPlayerEventBus
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.database.common.AudioBookDatabase
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.networkCacheDB.NetworkCacheDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataApi
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataService
import com.allsoftdroid.feature.book_details.data.repository.MetadataRepositoryImpl
import com.allsoftdroid.feature.book_details.data.repository.TrackListRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import com.allsoftdroid.feature.book_details.domain.usecase.GetDownloadUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetMetadataUsecase
import com.allsoftdroid.feature.book_details.domain.usecase.GetTrackListUsecase
import com.allsoftdroid.feature.book_details.presentation.utils.*
import com.allsoftdroid.feature.book_details.presentation.viewModel.BookDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object BookDetailsDI {
    val repositoryModule : Module = module {

        factory {
            MetadataRepositoryImpl(
                metadataDao = get(),
                bookId = getProperty(PROPERTY_BOOK_ID),
                metadataDataSource = get(),
                saveInDatabase = get(named(name = METADATA_DATABASE))) as IMetadataRepository
        }

        factory {
            FakeTrackListRepository() as ITrackListRepository
        }

        factory {
            SearchBookDetailsRepositoryImpl(storeCachingRepository = get(),
                bestMatcher = get()) as ISearchBookDetailsRepository
        }

        factory {
            FakeBookDetailsRepository() as IFetchAdditionBookDetailsRepository
        }

        single {
            UseCaseHandler.getInstance()
        }
    }

    val dataModule : Module = module {
        single {
            FakeMetadataSource() as MetadataDao
        }

        single {
            FakeNetworkCacheDao() as NetworkCacheDao
        }

        single(named(name = METADATA_DATABASE)) {
            FakeSaveInDatabase(dao = get()) as SaveInDatabase<MetadataDao,SaveMetadataInDatabase>
        }

        single {
            FakeRemoteMetadataSource() as ArchiveMetadataService
        }

        single{
            BestBookDetailsParser()
        }

        single {
            BookDetailsParserFromHtml()
        }

        single {
            DownloaderEventBus.getEventBusInstance()
        }

        single {
            AudioPlayerEventBus.getEventBusInstance()
        }

    }

    private const val PROPERTY_BOOK_ID = "bookDetails_book_id"
    private const val METADATA_DATABASE = "SaveMetadataInDatabase"
}