package com.taitsmith.busboy.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Bus: Serializable {
    @SerializedName("VehicleId")
    var vehicleId: Int? = null

    @SerializedName("CurrentTripId")
    var currentTripId: Int? = null

    @SerializedName("Latitude")
    var latitude: Double? = null

    @SerializedName("Longitude")
    var longitude: Double? = null

    @SerializedName("Heading")
    var heading: Int? = null

    @SerializedName("TimeLastReported")
    var timeLastReported: String? = null

    @SerializedName("IsActive")
    var isActive: Boolean? = null

    @SerializedName("Description")
    var description: String? = null

    @SerializedName("VehicleType")
    var vehicleType: String? = null

    @SerializedName("VehicleTypeDescription")
    var vehicleTypeDescription: String? = null

    @SerializedName("Make")
    var make: String? = null

    @SerializedName("SerialNumber")
    var serialNumber: String? = null

    @SerializedName("LicenseNumber")
    var licenseNumber: String? = null

    @SerializedName("Length")
    var length: String? = null

    @SerializedName("PropulsionType")
    var propulsionType: String? = null

    @SerializedName("HasWiFi")
    var hasWiFi: Boolean? = null

    @SerializedName("HasAC")
    var hasAC: Boolean? = null

    @SerializedName("StandingCapacity")
    var standingCapacity: String? = null

    @SerializedName("SeatingCapacity")
    var seatingCapacity: String? = null

    @SerializedName("LimitCapacity")
    var limitCapacity: String? = null

    //so we can save this to a db and keep track of all the buses we've ridden
    var hasRidden: Boolean? = false
}