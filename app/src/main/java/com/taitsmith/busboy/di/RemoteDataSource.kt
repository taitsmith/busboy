package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun predictions(s: String, r: String?): Flow<BustimeResponse>
    fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse>
    fun nearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>>
    //emits one Stop per input stop, populated with its lines-served string
    fun linesServedByStop(stops: List<Stop>): Flow<Stop>
    fun vehicleLocation(vid: String): Flow<Bus>
    suspend fun getDetailedBusInfo(vid: String): Bus
    suspend fun getBusRouteWaypoints(routeName: String): List<LatLng>
    suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng>


}