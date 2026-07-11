package com.taitsmith.busboy

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.taitsmith.busboy.data.BusboyDatabase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Verifies the handwritten [BusboyDatabase.MIGRATION_3_4]: a v3 favorite (single-column `stopId`
 * primary key, no agency) survives the rebuild into the v4 composite-key table and is stamped
 * `AC_TRANSIT`. Uses the exported schema JSONs under app/schemas (wired into androidTest assets in
 * build.gradle). Runs under connectedAndroidTest.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BusboyDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate3To4_preservesFavoritesAndStampsAgency() {
        helper.createDatabase(testDb, 3).apply {
            execSQL(
                "INSERT INTO Stop (stopId, name, latitude, longitude, linesServed) " +
                    "VALUES ('55555', 'Broadway & 25th St', 37.8, -122.2, '51A Fruitvale BART')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true, BusboyDatabase.MIGRATION_3_4)

        db.query("SELECT stopId, agency, name, linesServed FROM Stop").use { cursor ->
            assertThat(cursor.moveToFirst(), equalTo(true))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("stopId")), equalTo("55555"))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("agency")), equalTo("AC_TRANSIT"))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name")), equalTo("Broadway & 25th St"))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("linesServed")), equalTo("51A Fruitvale BART"))
            assertThat(cursor.count, equalTo(1))
        }
    }
}
