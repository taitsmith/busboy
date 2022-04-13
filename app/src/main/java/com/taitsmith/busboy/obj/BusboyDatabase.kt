package com.taitsmith.busboy.obj

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Stop::class, StopDestinationResponse.RouteDestination::class], version = 1)
abstract class BusboyDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDestinationDao
}