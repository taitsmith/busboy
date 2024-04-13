package com.taitsmith.busboy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Stop(

    @PrimaryKey
    @SerializedName("StopId")
    var stopId: String = "",

    @SerializedName("Name")
    var name: String? = null,

    @SerializedName("Latitude")
    var latitude: Double? = null,

    @SerializedName("Longitude")
    var longitude: Double? = null,

    var linesServed: String? = null
): Serializable