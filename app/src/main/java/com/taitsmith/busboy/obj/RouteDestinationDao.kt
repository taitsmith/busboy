package com.taitsmith.busboy.obj

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RouteDestinationDao {
    @Query("SELECT * FROM routedestination")
    fun getAll(): List<StopDestinationResponse.RouteDestination>

    @Insert
    fun insertAll(vararg routes: StopDestinationResponse.RouteDestination)

    @Delete
    fun delete(routeDestination: StopDestinationResponse.RouteDestination)
}