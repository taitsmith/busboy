package com.taitsmith.busboy.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.taitsmith.busboy.api.StopDestinationResponse

@Database(
    entities = [Stop::class, StopDestinationResponse.RouteDestination::class],
    version = 2,
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2
        )]
)
abstract class BusboyDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDestinationDao
}