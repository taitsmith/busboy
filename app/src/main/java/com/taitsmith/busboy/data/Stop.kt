package com.taitsmith.busboy.data

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * A favorited stop. The [agency] discriminator is part of the composite primary key so the same
 * numeric stop id can exist for both AC Transit and CTA without colliding. It never arrives from an
 * API payload (no [SerializedName]); [com.taitsmith.busboy.di.DatabaseRepository] stamps the active
 * agency when a stop is saved, and reads are filtered by it.
 */
@Entity(primaryKeys = ["stopId", "agency"])
data class Stop(

    @SerializedName("StopId")
    var stopId: String = "",

    var agency: String = Agency.AC_TRANSIT.name,

    @SerializedName("Name")
    var name: String? = null,

    @SerializedName("Latitude")
    var latitude: Double? = null,

    @SerializedName("Longitude")
    var longitude: Double? = null,

    var linesServed: String? = null
): Serializable
