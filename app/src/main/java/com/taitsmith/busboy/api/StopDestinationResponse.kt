package com.taitsmith.busboy.api

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * When we call the AC Transit API to get nearby stops, we also want to figure out which way those
 * stops go (NB/SB/EB/WB). The call to find nearby stops doesn't include this, so we need to make
 * a separate call for each stop returned to find which direction it goes.
 *
 */
class StopDestinationResponse {
    @SerializedName("StopId")
    var stopId: Int? = null

    @SerializedName("Status")
    var status: String? = null

    @SerializedName("RouteDestinations")
    var routeDestinations: List<RouteDestination>? = null

    @Entity
    data class RouteDestination (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo var id: Long? = null,

        @SerializedName("RouteId")//ex 51A
        var routeId: String?,

        @SerializedName("Direction")//ex Northbound
        var direction: String? = null,

        @SerializedName("Destination")//ex To Fruitvale BART
        var destination: String?,

        @SerializedName("Status")//for lines like 8XX and 6XX that have limited hours
        var status: String? = null
    )
}