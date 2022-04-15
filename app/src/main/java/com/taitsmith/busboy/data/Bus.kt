package com.taitsmith.busboy.data

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class Bus {
    @SerializedName("VehicleId")
    @Expose
    var vehicleId: Int? = null

    @SerializedName("CurrentTripId")
    @Expose
    var currentTripId: Int? = null

    @SerializedName("Latitude")
    @Expose
    var latitude: Double? = null

    @SerializedName("Longitude")
    @Expose
    var longitude: Double? = null

    @SerializedName("Heading")
    @Expose
    var heading: Int? = null

    @SerializedName("TimeLastReported")
    @Expose
    var timeLastReported: String? = null
}