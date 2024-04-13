package com.taitsmith.busboy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Stop(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo var id: Long? = null,

    @SerializedName("StopId")
    var stopId: String? = null,

    @SerializedName("Name")
    var name: String? = null,

    @SerializedName("Latitude")
    var latitude: Double? = null,

    @SerializedName("Longitude")
    var longitude: Double? = null,

    var linesServed: String? = null
): Serializable