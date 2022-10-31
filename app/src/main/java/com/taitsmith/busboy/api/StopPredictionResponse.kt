package com.taitsmith.busboy.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.taitsmith.busboy.data.Prediction

class StopPredictionResponse {
    @SerializedName("bustime-response")
    @Expose
    val bustimeResponse: BustimeResponse? = null

    class BustimeResponse {
        @SerializedName("prd")
        @Expose
        val prd: List<Prediction>? = null

        @SerializedName("error")
        @Expose
        val error: List<BusError>? = null

        class BusError {
            @SerializedName("rtpidatafeed")
            @Expose
            var rtpidatafeed: String? = null

            @SerializedName("stpid")
            @Expose
            var stpid: String? = null

            @SerializedName("msg")
            @Expose
            var msg: String? = null
        }
    }
}