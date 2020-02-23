package com.allsoftdroid.database.networkCacheDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface NetworkCacheDao {
    @Query("SELECT response FROM Network_Cache_Table where networkRequestId=:networkRequestId")
    fun getNetworkResponse(networkRequestId: String): Flow<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(networkResponseEntity: DatabaseNetworkResponseEntity)

    @Query("DELETE FROM Network_Cache_Table WHERE networkRequestId=:networkRequestId")
    suspend fun removeResponse(networkRequestId: String)

    @Query("DELETE FROM Network_Cache_Table")
    suspend fun removeAll()
}