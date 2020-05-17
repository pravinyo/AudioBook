package com.allsoftdroid.database.listenLaterDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.allsoftdroid.database.listenLaterDB.entity.DatabaseListenLaterEntity

@Dao
interface ListenLaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertForLater(listenLater : DatabaseListenLaterEntity)

    @Query("DELETE FROM ListenLater_Table where book_id = :identifier")
    fun removeById(identifier:String):Int

    @Query("SELECT * FROM ListenLater_Table order by timestamp desc")
    fun getBooksInLIFO():List<DatabaseListenLaterEntity>

    @Query("SELECT * FROM ListenLater_Table order by timestamp asc")
    fun getBooksInFIFO():List<DatabaseListenLaterEntity>

    @Query("SELECT * FROM ListenLater_Table order by play_time")
    fun getBooksInOrderOfLength():List<DatabaseListenLaterEntity>

    @Query("SELECT count(*) FROM ListenLater_Table where book_id = :bookId")
    fun getListenLaterStatusFor(bookId:String):Int
}