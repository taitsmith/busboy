package com.taitsmith.busboy.api

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import com.taitsmith.busboy.ui.MainActivity
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepository @Inject constructor(@AcTransitApiInterface
                                        val acTransitApiInterface: ApiInterface,
                                        @MapsApiInterface
                                        val mapsApiInterface: ApiInterface,
                                        remoteDataSource: RemoteDataSource
    ) {

    val stopPredictions: Flow<List<Prediction>> = remoteDataSource.predictions
        .map { predictions ->
            predictions.filter { it.dyn == 0 }
            predictions.onEach {
                if (it.prdctdn == "1" || it.prdctdn == "Due") it.prdctdn = "Arriving"
                else it.prdctdn = "in " + it.prdctdn + " minutes"
            }
        }

    suspend fun getBusLocation(vehicleId: String): Bus {
        val returnBus = acTransitApiInterface.getVehicleInfo(vehicleId)
        if (returnBus.latitude == null || returnBus.longitude == null) {
            throw Exception("null_coords")
        } else return acTransitApiInterface.getVehicleInfo(vehicleId)
    }

    suspend fun getDetailedBusInfo(vid: String): Bus {
        return acTransitApiInterface.getDetailedVehicleInfo(vid)[0]
    }

    /*
        The way AC Transit's API works, we have to make two calls to display everything on the
        'nearby' screen. One to find all nearby stops, and this one to get the list of lines served.
        Then we can smoosh everything into one string with a \n between each to display it. So
        that's whats going on here and in the following method
        https://api.actransit.org/transit/Help/Api/GET-stop-stopId-destinations
     */
    suspend fun getNearbyStops(lat: Double, lon: Double, distance: Int, isActive: Boolean, rt: String?)
        : List<Stop> {
        return acTransitApiInterface.getNearbyStops(lat, lon, distance, isActive, rt)
    }

    suspend fun getLinesServedByStop(stopList: List<Stop>): List<Stop> {
        for (stop in stopList) {
            MainActivity.mutableNearbyStatusUpdater.postValue(stop.name)
            val destinations = acTransitApiInterface.getStopDestinations(stop.stopId)
            val sb = StringBuilder()
            if (destinations.status == "No service today at this stop") { //ac transit's api is weird
                sb.append(destinations.status)                            //and will often show no service
            } else {                                                      //even when there's service
                destinations.routeDestinations?.forEach {
                    sb.append(it.routeId)
                        .append(" ")
                        .append(it.destination)
                        .append("\n")
                    stop.linesServed = sb.toString()
                }
            }
        }
        return stopList
    }

    //get service alerts for a given stop so we can inform users of delays, detours, stop closures etc
    suspend fun getServiceAlertsForStop(stopId: String) : ServiceAlertResponse {
       return acTransitApiInterface.getServiceAlertsForStop(stopId)
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