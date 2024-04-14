package com.taitsmith.busboy.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.taitsmith.busboy.api.StopDestinationResponse

@Database(
    entities = [Stop::class, StopDestinationResponse.RouteDestination::class],
    exportSchema = true,
    version = 3,
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2
        ),
        AutoMigration(
            from = 2,
            to = 3,
            spec = BusboyDatabase.Migration_2_3::class
        )
    ]
)
abstract class BusboyDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDestinationDao

    @DeleteColumn(tableName = "Stop", columnName = "id")
    class Migration_2_3 : AutoMigrationSpec
}