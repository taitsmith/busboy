package com.taitsmith.busboy.obj

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface RouteDestinationDao {
    @Query("SELECT * FROM routedestination")
    fun getAll(): List<StopDestinationResponse.RouteDestination>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg routes: StopDestinationResponse.RouteDestination)

    @Delete
    fun delete(routeDestination: StopDestinationResponse.RouteDestination)

    @Update
    fun updateRoutes(vararg routes: StopDestinationResponse.RouteDestination)
}