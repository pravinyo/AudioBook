package com.allsoftdroid.database.bookListDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * This DAO interface specifies what are the type of database operation can be done.
 * It act as the interface to query database
 */

@Dao
interface AudioBookDao {
    /**
     * Get List of {@link DatabaseAudioBook} from database
     */
    @Query("SELECT * FROM AudioBook_Table order by datetime(published_date) desc")
    fun getBooks(): Flow<List<DatabaseAudioBook>>

    /**
     * Get Database Book instance by username
     */
    @Query("SELECT * FROM AudioBook_Table WHERE id = :identifier")
    fun getBookBy(identifier: String): DatabaseAudioBook

    /**
     *
     * Insert individual book in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert( book : DatabaseAudioBook)

    /**
     * Insert list of books in the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllList(books : List<DatabaseAudioBook>)

    /**
     * Clear the tables
     */
    @Query("DELETE FROM AudioBook_Table")
    fun deleteAll():Int

    /**
     * Delete single entry from the table using book identifier
     */
    @Query("DELETE FROM AudioBook_Table WHERE id=:identifier")
    fun deleteItem(identifier: String)

    /**
     * Get last instance of user from the database
     */
    @Query("SELECT * FROM AudioBook_Table order by published_date desc limit 1")
    fun getLastBook(): DatabaseAudioBook
}