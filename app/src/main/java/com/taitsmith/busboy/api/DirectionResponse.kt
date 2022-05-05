package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName
import com.google.android.gms.maps.model.LatLng

class DirectionResponse {
    @SerializedName("geocoded_waypoints")
    var geoWaypoints: List<Any>? = null

    @SerializedName("routes")
    var routeList: List<MapRoute>? = null

    @SerializedName("status")
    var status: String? = null

    class MapRoute {
        @SerializedName("bounds")
        var boundsList: Any? = null

        @SerializedName("legs")
        var tripList: List<Leg>? = null

        @SerializedName("warnings")
        lateinit var warnings: Array<String>
    }

    class Leg {
        @SerializedName("steps")
        var stepList: List<Step>? = null
    }

    class Step {
        @SerializedName("end_location")
        var endCoords: EndCoords? = null
    }

    class EndCoords {
        @SerializedName("lat")
        var lat: Double? = null

        @SerializedName("lng")
        var lon: Double? = null
        fun returnCoords(): LatLng {
            return LatLng(lat!!, lon!!)
        }
    }
}