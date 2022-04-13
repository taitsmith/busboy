package com.taitsmith.busboy.obj

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Stop(
    @PrimaryKey val autoGenerate: Boolean,

    @SerializedName("StopId")
    var stopId: String? = null,

    @SerializedName("Name")
    var name: String? = null,

    @SerializedName("Latitude")
    var latitude: Double? = null,

    @SerializedName("Longitude")
    var longitude: Double? = null,

    var linesServed: String? = null
)