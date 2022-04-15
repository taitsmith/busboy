package com.taitsmith.busboy

import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.api.StopDestinationResponse.RouteDestination

object TestUtils {

    fun createStop() = Stop(
        1,
        stopId = "53929"
    )

    fun createLines() : List<RouteDestination> {
        val line1 = RouteDestination(
            1,
            routeId = "51A",
            destination = "To Fruitvale BART")
        val line2 = RouteDestination(
            2,
            routeId = "51A",
            destination = "To Rockridge BART")
        val line3 = RouteDestination(
            3,
            routeId = "6",
            destination = "To Downtown Oakland")


        return listOf(line1, line2, line3)
    }
}