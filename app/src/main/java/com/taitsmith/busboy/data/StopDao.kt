package com.taitsmith.busboy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Query("SELECT * FROM stop")
    fun getAll(): Flow<List<Stop>>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insertAll(vararg stops: Stop)

    @Delete
    fun delete(stop: Stop)

    @Update
    fun updateStops(vararg stops: Stop)

    @Query("DELETE FROM stop")
    fun deleteAll()

}