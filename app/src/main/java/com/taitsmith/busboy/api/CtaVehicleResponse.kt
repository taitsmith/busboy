package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName

data class CtaVehicleResponse(
    @SerializedName("bustime-response")
    val bustimeResponse: Body
) {
    class Body {
        @SerializedName("vehicle")
        var vehicle: List<CtaVehicle>? = null

        @SerializedName("error")
        var error: List<BustimeResponse.BusError>? = null
    }
}
