package com.taitsmith.busboy

import com.taitsmith.busboy.obj.Stop
import com.taitsmith.busboy.obj.StopDestinationResponse

object TestUtils {

    fun createStop() = Stop(
        true,
        stopId = "53929"
    )

    fun createLine() = StopDestinationResponse.RouteDestination(
        autoGenerate = true,
        routeId = "6",
        destination = "To Downtown Oakland"
    )
}