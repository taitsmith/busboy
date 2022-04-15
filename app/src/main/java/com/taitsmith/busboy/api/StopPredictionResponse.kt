package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

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

        class Prediction {
            @SerializedName("stpnm")
            @Expose
            val stpnm: String? = null

            @SerializedName("vid")
            @Expose
            val vid: String? = null

            @SerializedName("rt")
            @Expose
            val rt: String? = null

            @SerializedName("rtdir")
            @Expose
            val rtdir: String? = null

            @SerializedName("des")
            @Expose
            val des: String? = null

            @SerializedName("prdtm")
            @Expose
            val prdtm: String? = null

            @SerializedName("dyn")
            @Expose
            val dyn: Int? = null

            @SerializedName("prdctdn")
            @Expose
            var prdctdn: String? = null
        }
    }
}