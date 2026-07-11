package com.taitsmith.busboy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CtaStopDao {
    @Query("SELECT COUNT(*) FROM cta_stop")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<CtaStop>)

    //cheap bounding-box prefilter; the exact radius (Haversine) is applied in Kotlin afterwards
    @Query("SELECT * FROM cta_stop WHERE lat BETWEEN :minLat AND :maxLat AND lon BETWEEN :minLon AND :maxLon")
    suspend fun inBoundingBox(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): List<CtaStop>

    @Query("SELECT linesServed FROM cta_stop WHERE stopId = :stopId")
    suspend fun linesServedFor(stopId: String): String?
}
