package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    fun stopPredictions(stpId: String, route: String?): Flow<List<Prediction>>
    fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse>
    fun getNearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>>
    fun vehicleInfo(vid: String): Flow<Bus>
    fun getLinesServedByStops(stops: List<Stop>): Flow<Stop>
    suspend fun getDetailedBusInfo(vid: String): Bus
    suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng>
    suspend fun getBusRouteWaypoints(routeName: String): List<LatLng>
}