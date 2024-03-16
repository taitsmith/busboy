package com.taitsmith.busboy.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.taitsmith.busboy.data.Prediction

class StopPredictionResponse {
    @SerializedName("bustime-response")
    @Expose
    val bustimeResponse: BustimeResponse? = null
}