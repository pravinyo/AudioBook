package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.network.request.ILibriVoxApiService
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository.IStoreRepository
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.concurrent.TimeUnit

class NetworkCachingStoreRepositoryImpl(private val networkService: ILibriVoxApiService) : IStoreRepository {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun provideStoreMultiParam(): Store<Pair<String, Int>, String> {

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
            }.cachePolicy(
                MemoryPolicy.builder()
                    .setMemorySize(20)
                    .setExpireAfterAccess(10) // or setExpireAfterWrite(10)
                    .setExpireAfterTimeUnit(TimeUnit.HOURS)
                    .build()
            )
            .build()
    }
}