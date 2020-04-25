package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.ILibriVoxApiService
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.ILibriVoxDetailsApiService
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.allsoftdroid.database.networkCacheDB.DatabaseNetworkResponseEntity
import com.allsoftdroid.database.networkCacheDB.NetworkCacheDao
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.concurrent.TimeUnit

class NetworkCachingStoreRepositoryImpl(private val networkService: ILibriVoxApiService,
                                        private val bookDetailsService:ILibriVoxDetailsApiService,
                                        private val networkCacheDao: NetworkCacheDao) : IStoreRepository {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun provideEnhanceBookSearchStore(): Store<Pair<String, Int>, String> {

        return StoreBuilder
            .fromNonFlow<Pair<String,Int>, String> { (searchTitle,pageNumber) ->
                val response = networkService.searchBookInRemoteRepositoryAsync(
                    title = searchTitle,
                    author = "",
                    search_page = pageNumber
                ).await()

                if(response.isSuccessful){
                    response.body()?.results?:""
                }else{
                    ""
                }
            }.persister(
                reader = {
                    pair-> networkCacheDao.getNetworkResponse("${pair.first}#${pair.second}")
                },
                writer = {
                    pair,response ->
                    networkCacheDao.insertResponse(
                    DatabaseNetworkResponseEntity(identifier = "${pair.first}#${pair.second}",networkResponse = response))
                },
                delete = {
                        (title,page) -> networkCacheDao.removeResponse("$title#$page")
                },
                deleteAll = networkCacheDao::removeAll
            ).cachePolicy(
                MemoryPolicy.builder()
                    .setMemorySize(20)
                    .setExpireAfterAccess(10) // or setExpireAfterWrite(10)
                    .setExpireAfterTimeUnit(TimeUnit.HOURS)
                    .build()
            )
            .build()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun provideEnhanceBookDetailsStore(): Store<String, String> {
        return StoreBuilder
            .fromNonFlow<String, String> { url ->
                val response = bookDetailsService.getBookDetailsPageAsync(url)
                response?:""
            }.persister(
                reader = {
                        url-> networkCacheDao.getNetworkResponse(url)
                },
                writer = {
                        url,response ->
                    networkCacheDao.insertResponse(
                        DatabaseNetworkResponseEntity(identifier = url,networkResponse = response))
                },
                delete = {
                        url -> networkCacheDao.removeResponse(url)
                },
                deleteAll = networkCacheDao::removeAll
            ).cachePolicy(
                MemoryPolicy.builder()
                    .setMemorySize(20)
                    .setExpireAfterAccess(10) // or setExpireAfterWrite(10)
                    .setExpireAfterTimeUnit(TimeUnit.HOURS)
                    .build()
            )
            .build()
    }
}