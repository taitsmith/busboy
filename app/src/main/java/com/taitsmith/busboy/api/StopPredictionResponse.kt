package com.taitsmith.busboy.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StopPredictionResponse(
    @SerializedName("bustime-response")
    @Expose
    val bustimeResponse: BustimeResponse
)