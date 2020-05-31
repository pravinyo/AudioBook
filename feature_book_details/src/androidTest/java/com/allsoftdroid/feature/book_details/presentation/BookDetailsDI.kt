package com.allsoftdroid.feature.book_details.presentation

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IFetchAdditionBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.ISearchBookDetailsRepository
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BestBookDetailsParser
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.utils.BookDetailsParserFromHtml
import com.allsoftdroid.common.base.store.audioPlayer.AudioPlayerEventBus
import com.allsoftdroid.common.base.store.downloader.DownloaderEventBus
import com.allsoftdroid.common.base.store.userAction.UserActionEventBus
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.common.base.utils.LocalFilesForBook
import com.allsoftdroid.database.common.SaveInDatabase
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.networkCacheDB.NetworkCacheDao
import com.allsoftdroid.feature.book_details.data.databaseExtension.SaveMetadataInDatabase
import com.allsoftdroid.feature.book_details.data.network.service.ArchiveMetadataService
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.domain.repository.IListenLaterRepository
import com.allsoftdroid.feature.book_details.domain.repository.IMetadataRepository
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import com.allsoftdroid.feature.book_details.presentation.utils.*
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object BookDetailsDI {
    val repositoryModule : Module = module {

        factory {
            FakeMetadataRepository(bookId = getProperty(PROPERTY_BOOK_ID)) as IMetadataRepository
        }

        factory {
            FakeTrackListRepository() as ITrackListRepository
        }

        factory {
            FakeSearchBookDetailsRepository() as ISearchBookDetailsRepository
        }

        factory {
            FakeBookDetailsRepository() as IFetchAdditionBookDetailsRepository
        }

        factory {
            FakeListenLaterRepository() as IListenLaterRepository
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

        single {
            UserActionEventBus.getEventBusInstance()
        }
    }

    private const val PROPERTY_BOOK_ID = "bookDetails_book_id"
    private const val METADATA_DATABASE = "SaveMetadataInDatabase"
}