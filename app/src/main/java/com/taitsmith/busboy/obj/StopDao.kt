package com.taitsmith.busboy.obj

import androidx.room.*

@Dao
interface StopDao {
    @Query("SELECT * FROM stop")
    fun getAllStops(): List<Stop>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insertAll(vararg stops: Stop)

    @Delete
    fun delete(stop: Stop)
}