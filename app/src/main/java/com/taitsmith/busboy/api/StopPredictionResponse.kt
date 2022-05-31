package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.taitsmith.busboy.data.Prediction

class StopPredictionResponse {
    @SerializedName("bustime-response")
    @Expose
    val bustimeResponse: BustimeResponse? = null

    @SerializedName("error")
    @Expose
    val errors: BusError? = null

    class BusError {
        @SerializedName("msg")
        var message: String? = null
    }

    class BustimeResponse {
        @SerializedName("prd")
        @Expose
        val prd: List<Prediction>? = null
    }
}