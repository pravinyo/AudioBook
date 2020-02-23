package com.allsoftdroid.database.common

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.allsoftdroid.database.bookListDB.AudioBookDao
import com.allsoftdroid.database.bookListDB.DatabaseAudioBook
import com.allsoftdroid.database.metadataCacheDB.MetadataDao
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseAlbumEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseMetadataEntity
import com.allsoftdroid.database.metadataCacheDB.entity.DatabaseTrackEntity
import com.allsoftdroid.database.networkCacheDB.DatabaseNetworkResponseEntity
import com.allsoftdroid.database.networkCacheDB.NetworkCacheDao

/**
 * Contains Database definition
 */
@Database(
    entities = [
        DatabaseAudioBook::class,
        DatabaseMetadataEntity::class,
        DatabaseAlbumEntity::class,
        DatabaseTrackEntity::class,
        DatabaseNetworkResponseEntity::class
    ],
    version = 6 ,
    exportSchema = false)
abstract class AudioBookDatabase : RoomDatabase(){

    //Books DAO
    abstract fun audioBooksDao() : AudioBookDao

    //Metadata DAO
    abstract fun metadataDao() : MetadataDao

    //NetworkCache DAO
    abstract fun networkDao(): NetworkCacheDao


    /**
     * Uses singleton approach to avoid multiple instance creation
     */
    companion object {

        @Volatile
        private var INSTANCE : AudioBookDatabase? = null

        fun getDatabase(context: Context): AudioBookDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioBookDatabase::class.java,
                    "audio_book_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}