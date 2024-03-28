package com.taitsmith.busboy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taitsmith.busboy.api.StopDestinationResponse

@Dao
interface RouteDestinationDao {
    @Query("SELECT * FROM routedestination")
    fun getAll(): List<StopDestinationResponse.RouteDestination>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg routes: StopDestinationResponse.RouteDestination)

    @Delete
    fun delete(routeDestination: StopDestinationResponse.RouteDestination)

    @Update
    fun updateRoutes(vararg routes: StopDestinationResponse.RouteDestination)
}