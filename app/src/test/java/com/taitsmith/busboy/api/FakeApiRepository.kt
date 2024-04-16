package com.taitsmith.busboy.api

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.ServiceAlert
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.ApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeApiRepository : ApiRepository {

    override fun stopPredictions(stpId: String, route: String?) = flow {
        val prediction1 = Prediction(
            "Broadway & 25th St",
            "1745",
            "51A",
            "to Fruitvale BART",
            "Fruitvale BART",
            "20230420 16:20"
        )

        val prediction2 = Prediction(
            "Broadway & 25th St",
            "1745",
            "51A",
            "to Fruitvale BART",
            "Fruitvale BART",
            "20230420 16:40"
        )
        emit(listOf(prediction1, prediction2))
    }

    override fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse> = flow {
        val alert = ServiceAlert()
        val btr = BustimeResponse()
        alert.nm = "Test alert"
        alert.prty = "Low"
        btr.sb = listOf(alert)
        emit(ServiceAlertResponse(btr))
    }

    override fun getNearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> = flow {
        val stop1 = Stop()
        stop1.stopId = "55555"
        stop1.name = "Fake Stop 1"

        val stop2 = Stop()
        stop2.stopId = "56669"
        stop2.name = "Fake Stop 2"

        emit(listOf(stop1, stop2))
    }

    override fun vehicleLocation(vid: String): Flow<Bus> = flow {
        emit(createMockedBus())
        delay(1000)
        emit(Bus())
    }

    override fun getLinesServedByStops(stops: List<Stop>): Flow<Stop> = flow {
        val stop1 = Stop()
        stop1.stopId = "55555"
        stop1.name = "Fake Stop 1"
        emit(stop1)
    }

    override suspend fun getDetailedBusInfo(vid: String): Bus {
        val mockedBus = Bus()
        mockedBus.vehicleId = 1529
        mockedBus.currentTripId = 2837
        mockedBus.hasAC = false
        mockedBus.hasWiFi = false
        mockedBus.latitude = 84.23
        mockedBus.longitude = -122.23
        return mockedBus
    }

    override suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng> {
        return listOf(
            LatLng(1.1, 1.1)
        )
    }

    override suspend fun getBusRouteWaypoints(routeName: String): List<LatLng> {
        return listOf(
            LatLng(1.1, 1.1)
        )
    }

    fun createMockedBus(): Bus {
        val mockedBus = Bus()
        mockedBus.vehicleId = 1529
        mockedBus.currentTripId = 2837
        mockedBus.hasAC = false
        mockedBus.hasWiFi = false
        mockedBus.latitude = 84.23
        mockedBus.longitude = -122.23

        return mockedBus
    }
}