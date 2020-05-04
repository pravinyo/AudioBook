package com.allsoftdroid.feature.book_details.presentation.utils

import com.allsoftdroid.database.networkCacheDB.DatabaseNetworkResponseEntity
import com.allsoftdroid.database.networkCacheDB.NetworkCacheDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeNetworkCacheDao : NetworkCacheDao {

    private var hashMap =  HashMap<String,String>()

    override fun getNetworkResponse(networkRequestId: String): Flow<String> {
        return flowOf(hashMap[networkRequestId]?:"")
    }

    override suspend fun insertResponse(networkResponseEntity: DatabaseNetworkResponseEntity) {
        hashMap[networkResponseEntity.identifier] = networkResponseEntity.networkResponse
    }

    override suspend fun removeResponse(networkRequestId: String) {
        hashMap.remove(networkRequestId)
    }

    override suspend fun removeAll() {
        hashMap.clear()
    }
}