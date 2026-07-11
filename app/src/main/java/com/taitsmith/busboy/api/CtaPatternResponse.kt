package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName

/**
 * CTA getpatterns response: one or more patterns (ptr), each a sequence of geo points (pt). Used to
 * build a route polyline. A stop point has typ "S", a plain waypoint typ "W"; both carry lat/lon.
 */
data class CtaPatternResponse(
    @SerializedName("bustime-response")
    val bustimeResponse: Body
) {
    class Body {
        @SerializedName("ptr")
        var ptr: List<Pattern>? = null

        @SerializedName("error")
        var error: List<BustimeResponse.BusError>? = null
    }

    class Pattern {
        @SerializedName("pid")   var pid: Int? = null
        @SerializedName("ln")    var ln: Double? = null
        @SerializedName("rtdir") var rtdir: String? = null
        @SerializedName("pt")    var pt: List<Point>? = null
    }

    class Point {
        @SerializedName("seq") var seq: Int? = null
        @SerializedName("lat") var lat: Double? = null
        @SerializedName("lon") var lon: Double? = null
        @SerializedName("typ") var typ: String? = null
    }
}
