package com.taitsmith.busboy.api

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepository @Inject constructor(@AcTransitApiInterface
                                        val acTransitApiInterface: ApiInterface,
                                        @MapsApiInterface
                                        val mapsApiInterface: ApiInterface,
                                        private val remoteDataSource: AcTransitRemoteDataSource
    ) {

    fun stopPredictions(stpId: String, route: String?): Flow<List<Prediction>> = remoteDataSource.predictions(stpId, route)
        .map { response ->
            if (!response.error.isNullOrEmpty()) {
                if (response.error[0].msg.equals("No service scheduled")) {
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

    fun serviceAlerts(stpid: String) = remoteDataSource.serviceAlerts(stpid)

    fun getNearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> =
        remoteDataSource.nearbyStops(
            latLng,
            distance,
            route
        )

    fun vehicleInfo(vid: String): Flow<Bus> = remoteDataSource.vehicleLocation(vid)
        .map {
            if (it.latitude == null) throw Exception("NULL_BUS_COORDS")
            else return@map it
        }

    fun getLinesServedByStops(stops: List<Stop>): Flow<Stop> = remoteDataSource.linesServedByStop(stops)
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

    suspend fun getDetailedBusInfo(vid: String): Bus {
        return acTransitApiInterface.getDetailedVehicleInfo(vid)[0]
    }

    //get walking directions from current location to a bus stop. google returns a ton of information
    //and you have to dig through the list to get what we want: a collection of lat/lon points
    //to draw a polyline on our map to represent walking directions
    suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng> {
        val polylineCoords: MutableList<LatLng> = ArrayList()

        val directionResponse = mapsApiInterface.getNavigationToStop(
            start, stop, "walking")

        //too many damn lists
        val stepList = directionResponse.routeList?.get(0)?.tripList?.get(0)?.stepList

        stepList?.forEach {
            it.endCoords?.returnCoords()?.let { it1 -> polylineCoords.add(it1) }
        }

        return polylineCoords
    }

    //get a list of lat/lon points so we can create a polyline of the selected bus route
    //and then display it on a map along with the current location of the bus
    suspend fun getBusRouteWaypoints(routeName: String): List<LatLng> {
        val polylineCoords: MutableList<LatLng> = ArrayList()

        val waypointResponse = acTransitApiInterface.getBusRouteWaypoints(routeName)

        if (waypointResponse.isEmpty()) throw Exception("empty_response")

        //need to go through several layers to get the good stuff
        waypointResponse[0].patterns?.get(0)?.waypoints?.forEach {
            polylineCoords.add(it.latLng)
        }

        return  polylineCoords
    }
}