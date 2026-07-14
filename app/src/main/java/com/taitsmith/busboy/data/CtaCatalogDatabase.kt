package com.taitsmith.busboy.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Read-only catalog of CTA bus stops, seeded once from the bundled asset (see CtaStopCatalog).
 * Kept separate from busboy_database so the ~10.6k catalog rows never entangle with the favorites
 * schema or its migrations. exportSchema is off — this DB is disposable and reseeded from the asset.
 */
@Database(entities = [CtaStop::class], version = 1, exportSchema = false)
abstract class CtaCatalogDatabase : RoomDatabase() {
    abstract fun ctaStopDao(): CtaStopDao
}
