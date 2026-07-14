package com.taitsmith.busboy.di

import android.content.Context
import com.taitsmith.busboy.data.CtaStop
import com.taitsmith.busboy.data.CtaStopDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * On-device access to the bundled CTA stop catalog. Seeds the Room catalog from the TSV asset on
 * first use (idempotent), then answers radius queries with a SQL bounding-box prefilter plus an
 * exact Haversine filter/sort in Kotlin. The parsing and geo math are in the companion so they can
 * be unit-tested without Room.
 */
@Singleton
class CtaStopCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ctaStopDao: CtaStopDao
) {
    private val seedMutex = Mutex()

    suspend fun nearbyStops(lat: Double, lon: Double, distanceMeters: Double, route: String?): List<CtaStop> {
        ensureSeeded()
        val latDelta = distanceMeters / METERS_PER_DEGREE
        val lonDelta = distanceMeters / (METERS_PER_DEGREE * cos(Math.toRadians(lat)))
        val rows = ctaStopDao.inBoundingBox(lat - latDelta, lat + latDelta, lon - lonDelta, lon + lonDelta)
        return filterAndSort(rows, lat, lon, distanceMeters, route)
    }

    suspend fun linesServedFor(stopId: String): String? {
        ensureSeeded()
        return ctaStopDao.linesServedFor(stopId)
    }

    private suspend fun ensureSeeded() {
        if (ctaStopDao.count() > 0) return
        seedMutex.withLock {
            if (ctaStopDao.count() > 0) return
            val stops = withContext(Dispatchers.IO) {
                context.assets.open(ASSET).bufferedReader().useLines { lines ->
                    lines.mapNotNull { parseLine(it) }.toList()
                }
            }
            ctaStopDao.insertAll(stops)
        }
    }

    companion object {
        private const val ASSET = "cta_stops.tsv"
        private const val METERS_PER_DEGREE = 111_320.0
        private const val EARTH_RADIUS_M = 6_371_000.0

        fun parseLine(line: String): CtaStop? {
            val c = line.split('\t')
            if (c.size < 5) return null
            val lat = c[2].toDoubleOrNull() ?: return null
            val lon = c[3].toDoubleOrNull() ?: return null
            return CtaStop(stopId = c[0], name = c[1], lat = lat, lon = lon, linesServed = c[4])
        }

        fun filterAndSort(
            rows: List<CtaStop>, lat: Double, lon: Double, distanceMeters: Double, route: String?
        ): List<CtaStop> {
            val routeFilter = route?.takeIf { it.isNotBlank() }
            return rows
                .mapNotNull { stop ->
                    if (routeFilter != null && !servesRoute(stop, routeFilter)) return@mapNotNull null
                    val d = haversineMeters(lat, lon, stop.lat, stop.lon)
                    if (d <= distanceMeters) stop to d else null
                }
                .sortedBy { it.second }
                .map { it.first }
        }

        private fun servesRoute(stop: CtaStop, route: String): Boolean =
            stop.linesServed.split(", ").any { it == route }

        fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            return 2 * EARTH_RADIUS_M * asin(min(1.0, sqrt(a)))
        }
    }
}
