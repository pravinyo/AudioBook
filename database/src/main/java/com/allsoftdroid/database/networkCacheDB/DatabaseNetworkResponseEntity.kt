package com.allsoftdroid.database.networkCacheDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Network_Cache_Table")
data class DatabaseNetworkResponseEntity(
    @PrimaryKey
    @ColumnInfo(name = "networkRequestId")
    var identifier : String,

    @ColumnInfo(name = "response")
    var networkResponse:String
)