package com.taitsmith.busboy.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A CTA bus stop from the bundled GTFS catalog (app/src/main/assets/cta_stops.tsv). CTA has no
 * geo-radius stop endpoint, so this table backs an on-device "nearby stops" search. [stopId] is
 * directly usable as a BusTracker stpid. [linesServed] is precomputed offline. Lives in its own
 * read-only catalog DB, separate from the user's favorites.
 */
@Entity(tableName = "cta_stop", indices = [Index("lat"), Index("lon")])
data class CtaStop(
    @PrimaryKey val stopId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val linesServed: String
)
