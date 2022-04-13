package com.taitsmith.busboy.obj

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

/**
 * When we call the AC Transit API to get nearby stops, we also want to figure out which way those
 * stops go (NB/SB/EB/WB). The call to find nearby stops doesn't include this, so we need to make
 * a separate call for each stop returned to find which direction it goes.
 *
 */
class StopDestinationResponse {
    @SerializedName("StopId")
    @Expose
    var stopId: Int? = null

    @SerializedName("Status")
    @Expose
    var status: String? = null

    @SerializedName("RouteDestinations")
    @Expose
    var routeDestinations: List<RouteDestination>? = null

    @Entity
    class RouteDestination {
        @SerializedName("RouteId")//ex 51A
        @Expose
        var routeId: String? = null

        @SerializedName("DirectionId")
        @Expose
        var directionId: Int? = null

        @SerializedName("Direction")//ex Northbound
        @Expose
        var direction: String? = null

        @SerializedName("Destination")//ex To Fruitvale BART
        @Expose
        var destination: String? = null

        @SerializedName("FinalPassingTime")
        @Expose
        var finalPassingTime: String? = null

        @SerializedName("Status")
        @Expose
        var status: String? = null
    }
}