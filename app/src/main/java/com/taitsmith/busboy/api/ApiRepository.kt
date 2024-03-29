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
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepository @Inject constructor(@AcTransitApiInterface
                                        val acTransitApiInterface: ApiInterface,
                                        @MapsApiInterface
                                        val mapsApiInterface: ApiInterface,
                                        remoteDataSource: AcTransitRemoteDataSource
    ) {

    val stopPredictions: Flow<List<Prediction>> = remoteDataSource.predictions
        .map { predictions ->
            predictions.filter { it.dyn == 0 } //whatever 'dyn' means, a non-zero indicates the bus isn't coming
            predictions.onEach {
                if (it.prdctdn == "1" || it.prdctdn == "Due") it.prdctdn = "Arriving"
                else it.prdctdn = "in " + it.prdctdn + " minutes"
            }
        }

    val serviceAlerts: Flow<ServiceAlertResponse> = remoteDataSource.serviceAlerts

    val nearbyStops: Flow<List<Stop>> = remoteDataSource.nearbyStops

    val nearbyStopsWithLines: Flow<Stop> = remoteDataSource.nearbyLinesServed

    val vehicleInfo: Flow<Bus> = remoteDataSource.vehicleLocation

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