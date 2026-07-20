package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
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
                //AC Transit says "No service scheduled"; CTA's BusTracker phrases the same no-buses
                //condition differently ("No arrival times", "No data found"). Map any of them to the
                //friendly no-service state rather than a generic "unknown error".
                val msg = response.error!![0].msg.orEmpty()
                if (msg.contains("no service", ignoreCase = true) ||
                    msg.contains("no arrival", ignoreCase = true) ||
                    msg.contains("no data", ignoreCase = true)
                ) {
                    throw Exception("NO_SERVICE_SCHEDULED")
                } else throw Exception("UNKNOWN")
            } else {
                //a non-zero 'dyn' means the bus isn't stopping, so drop it. treat a null/unknown
                //dyn as stopping so we don't hide predictions the api didn't tag.
                return@map response.prd.orEmpty()
                    .filter { (it.dyn ?: 0) == 0 }
                    .onEach {
                        //AC Transit returns "Due"; CTA returns "DUE" — treat both, case-insensitively.
                        if (it.prdctdn == "1" || it.prdctdn.equals("Due", ignoreCase = true)) it.prdctdn = "Arriving"
                        else it.prdctdn = "in " + it.prdctdn + " minutes"
                    }
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

    //each RemoteDataSource populates lines-served in its own way (AC via a per-stop call, CTA from
    //the bundled catalog), so this is a straight passthrough.
    override fun getLinesServedByStops(stops: List<Stop>): Flow<Stop> = remoteDataSource.linesServedByStop(stops)

    override suspend fun getDetailedBusInfo(vid: String) = remoteDataSource.getDetailedBusInfo(vid)


    override suspend fun getDirectionsToStop(start: String, stop: String) =
        remoteDataSource.getDirectionsToStop(start, stop)


    override suspend fun getBusRouteWaypoints(routeName: String) =
        remoteDataSource.getBusRouteWaypoints(routeName)
}