package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
    ) : ApiRepository {

    override fun stopPredictions(stpId: String, route: String?): Flow<List<Prediction>> = remoteDataSource.predictions(stpId, route)
        .map { response ->
            if (!response.error.isNullOrEmpty()) {
                if (response.error!![0].msg.equals("No service scheduled")) {
                    throw Exception("NO_SERVICE_SCHEDULED")
                } else throw Exception("UNKNOWN")
            } else {
                response.prd?.filter { it.dyn == 0 } //whatever 'dyn' means, a non-zero value means the bus isn't stopping
                response.prd?.onEach {
                    if (it.prdctdn == "1" || it.prdctdn == "Due") it.prdctdn = "Arriving"
                    else it.prdctdn = "in " + it.prdctdn + " minutes"
                }
                return@map response.prd!!
            }
        }

    override fun serviceAlerts(stpid: String) = remoteDataSource.serviceAlerts(stpid)

    override fun getNearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> =
        remoteDataSource.nearbyStops(
            latLng,
            distance,
            route
        )

    override fun vehicleLocation(vid: String): Flow<Bus> = remoteDataSource.vehicleLocation(vid)
        .map {
            if (it.latitude == null) throw Exception("NULL_BUS_COORDS")
            else return@map it
        }

    override fun getLinesServedByStops(stops: List<Stop>): Flow<Stop> = remoteDataSource.linesServedByStop(stops)
        .filter { !it.routeDestinations.isNullOrEmpty() }
        .map { response ->
            val sb = StringBuilder()
            response.routeDestinations?.forEach {
                sb.append(it.routeId)
                    .append(" ")
                    .append(it.destination)
                    .append("\n")
            }
            return@map Stop(
                name = response.stopName,
                stopId = response.stopId.toString(),
                linesServed = sb.toString()
            )
        }

    override suspend fun getDetailedBusInfo(vid: String) = remoteDataSource.getDetailedBusInfo(vid)


    override suspend fun getDirectionsToStop(start: String, stop: String) =
        remoteDataSource.getDirectionsToStop(start, stop)


    override suspend fun getBusRouteWaypoints(routeName: String) =
        remoteDataSource.getBusRouteWaypoints(routeName)
}