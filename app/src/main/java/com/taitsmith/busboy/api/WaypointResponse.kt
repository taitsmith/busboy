package com.taitsmith.busboy.api

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WaypointResponse {
    @SerializedName("Patterns")
    @Expose
    val patterns: List<Pattern>? = null

    class Pattern {
        @SerializedName("Waypoints")
        @Expose
        val waypoints: List<Waypoint>? = null

        class Waypoint {
            @SerializedName("Latitude")
            @Expose
            private val latitude: Double? = null

            @SerializedName("Longitude")
            @Expose
            private val longitude: Double? = null

            val latLng: LatLng
                get() = LatLng(latitude!!, longitude!!)
        }
    }
}