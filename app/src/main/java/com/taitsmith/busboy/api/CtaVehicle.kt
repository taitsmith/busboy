package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName
import com.taitsmith.busboy.data.Bus

/**
 * A single vehicle from CTA's getvehicles response. CTA returns a terser shape than AC Transit
 * (no make / wifi / ac / capacity), so [toBus] fills the domain [Bus] fields it can and leaves the
 * rest null. lat/lon/hdg arrive as strings in CTA's JSON.
 */
class CtaVehicle {
    @SerializedName("vid")    var vid: String? = null
    @SerializedName("lat")    var lat: String? = null
    @SerializedName("lon")    var lon: String? = null
    @SerializedName("hdg")    var hdg: String? = null
    @SerializedName("des")    var des: String? = null
    @SerializedName("rt")     var rt: String? = null
    @SerializedName("dly")    var dly: Boolean? = null
    @SerializedName("tmstmp") var tmstmp: String? = null

    fun toBus(): Bus = Bus().apply {
        vehicleId = vid?.toIntOrNull()
        latitude = lat?.toDoubleOrNull()
        longitude = lon?.toDoubleOrNull()
        heading = hdg?.toIntOrNull()
        description = des
        timeLastReported = tmstmp
        isActive = true
    }
}
