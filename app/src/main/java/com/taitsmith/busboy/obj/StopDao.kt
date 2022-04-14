package com.taitsmith.busboy.obj

import androidx.room.*

@Dao
interface StopDao {
    @Query("SELECT * FROM stop")
    fun getAll(): List<Stop>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insertAll(vararg stops: Stop)

    @Delete
    fun delete(stop: Stop)

    @Update
    fun updateStops(vararg stops: Stop)
}