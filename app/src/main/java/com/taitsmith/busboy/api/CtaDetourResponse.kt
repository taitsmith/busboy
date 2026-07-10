package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName

data class CtaDetourResponse(
    @SerializedName("bustime-response")
    val bustimeResponse: Body
) {
    class Body {
        @SerializedName("dtrs")
        var dtrs: List<CtaDetour>? = null

        @SerializedName("error")
        var error: List<BustimeResponse.BusError>? = null
    }
}
