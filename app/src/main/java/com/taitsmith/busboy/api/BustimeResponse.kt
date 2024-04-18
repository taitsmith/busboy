package com.taitsmith.busboy.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.ServiceAlert

/**
 * bustime-response is a general class returned from ac transit prediction api calls as well as
 * ac transit service alert api calls.
 */
class BustimeResponse {
    //body returned for stop prediction calls
    @SerializedName("prd")
    @Expose
    var prd: List<Prediction>? = null

    //body returned for service alert calls
    @SerializedName("sb")
    @Expose
    var sb: List<ServiceAlert>? = null

    //almost always empty
    @SerializedName("error")
    @Expose
    var error: List<BusError>? = null

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