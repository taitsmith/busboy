package com.taitsmith.busboy.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taitsmith.busboy.api.StopDestinationResponse

@Database(
    entities = [Stop::class, StopDestinationResponse.RouteDestination::class],
    exportSchema = true,
    version = 4,
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

    companion object {
        /**
         * Adds the [Stop.agency] column and folds it into a composite primary key
         * (`stopId`, `agency`). Changing the primary key is not AutoMigration-able, so the table is
         * rebuilt by hand: existing favorites predate CTA and are all AC Transit, so they are stamped
         * `AC_TRANSIT` on the way across. Column order/affinity/nullability mirror what Room generates
         * for the v4 [Stop] entity so post-migration schema validation passes.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Stop_new` (" +
                        "`stopId` TEXT NOT NULL, " +
                        "`agency` TEXT NOT NULL, " +
                        "`name` TEXT, " +
                        "`latitude` REAL, " +
                        "`longitude` REAL, " +
                        "`linesServed` TEXT, " +
                        "PRIMARY KEY(`stopId`, `agency`))"
                )
                db.execSQL(
                    "INSERT INTO `Stop_new` (`stopId`, `agency`, `name`, `latitude`, `longitude`, `linesServed`) " +
                        "SELECT `stopId`, 'AC_TRANSIT', `name`, `latitude`, `longitude`, `linesServed` FROM `Stop`"
                )
                db.execSQL("DROP TABLE `Stop`")
                db.execSQL("ALTER TABLE `Stop_new` RENAME TO `Stop`")
            }
        }
    }
}
