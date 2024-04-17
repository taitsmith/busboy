package com.taitsmith.busboy.api

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.ServiceAlert
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRemoteDataSource : RemoteDataSource {

    override fun predictions(s: String, r: String?): Flow<BustimeResponse> = flow {
        val btr = BustimeResponse()

        if (s == "good") {
            btr.prd = listOf(
                Prediction(
                    stpnm = "Broadway & 25th St",
                    rt = "51A",
                    vid = "1832",
                    prdctdn = "10"
                )
            )
        } else {
            val error = BustimeResponse.BusError()
            error.msg = "No service scheduled"
            btr.error = listOf(error)
        }
        emit(btr)
    }

    override fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse> = flow {
        val btr = BustimeResponse()
        val sa = ServiceAlert()
        sa.nm = "fake service alert"
        btr.sb = listOf(sa)
        emit(ServiceAlertResponse(btr))
    }

    override fun nearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> = flow {
        val stop = Stop()
        stop.stopId = "55555"
        stop.name = "Fake stop"
        emit(listOf(stop))
    }

    override fun linesServedByStop(stops: List<Stop>): Flow<StopDestinationResponse> = flow {
        stops.forEach {
            val sdr = StopDestinationResponse()
            if (it.name.equals("good")) {
                sdr.routeDestinations = listOf(
                    StopDestinationResponse.RouteDestination(
                        destination = "to good destination",
                        id = 2345,
                        routeId = "51A",
                    )
                )
                emit(sdr)
            } else emit(sdr)
        }
    }

    override fun vehicleLocation(vid: String): Flow<Bus> = flow {
        val mockedBus = Bus()
        mockedBus.vehicleId = 1529
        mockedBus.currentTripId = 2837
        mockedBus.hasAC = false
        mockedBus.hasWiFi = false

        if (vid == "1234") {
            mockedBus.latitude = 84.23
            mockedBus.longitude = -122.23
        } else {
            mockedBus.latitude = null
            mockedBus.longitude = null
        }

        emit(mockedBus)
    }

    override suspend fun getDetailedBusInfo(vid: String): Bus {
        val mockedBus = Bus()
        mockedBus.vehicleId = 1529
        mockedBus.currentTripId = 2837
        mockedBus.hasAC = false
        mockedBus.hasWiFi = false

        return mockedBus
    }

    override suspend fun getBusRouteWaypoints(routeName: String): List<LatLng> {
        return listOf(LatLng(1.1, 1.1))
    }

    override suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng> {
        return listOf(LatLng(1.1, 1.1))
    }

}