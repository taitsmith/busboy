package com.taitsmith.busboy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Stop(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo var id: Long,

    @SerializedName("StopId")
    var stopId: String,

    @SerializedName("Name")
    var name: String,

    @SerializedName("Latitude")
    var latitude: Double,

    @SerializedName("Longitude")
    var longitude: Double,

    var linesServed: String? = null
): Serializable