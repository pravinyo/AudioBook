package com.allsoftdroid.feature_book.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Contains Database definition
 */
@Database(entities = [DatabaseAudioBook::class],version = 2 ,exportSchema = false)
abstract class AudioBookDatabase : RoomDatabase(){

    //Books DAO
    abstract fun audioBooksDao() : AudioBookDao


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