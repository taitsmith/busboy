package com.taitsmith.busboy.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ServiceAlertResponse(
    @SerializedName("bustime-response")
    @Expose
    var bustimeResponse: BustimeResponse
) : Serializable
