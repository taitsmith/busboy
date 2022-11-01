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
import kotlinx.coroutines.*
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepository @Inject constructor(@AcTransitApiInterface
                                        val acTransitApiInterface: ApiInterface,
                                        @MapsApiInterface
                                        val mapsApiInterface: ApiInterface) {

    suspend fun getStopPredictions(stopId: String, rt: String?): List<Prediction> {
        val predictionList = mutableListOf<Prediction>()

        runBlocking {
            val predictionResponseList = acTransitApiInterface.getStopPredictionList(stopId, rt)

            //AC Transit changed their API responses again, so even an invalid stop will
            //return a 200/OK, so we have to check for an error message now.
            if (predictionResponseList.bustimeResponse?.error != null) {
                when (predictionResponseList.bustimeResponse.error[0].msg) {
                    "No service scheduled" -> throw Exception("no_service")
                    "No data found for parameter" -> throw Exception("no_data")
                }
            }

            predictionResponseList.bustimeResponse?.prd?.forEach {
                if (it.dyn == 0) { //non-zero dyn means cancelled or not stopping
                    if (it.prdctdn == "1" || it.prdctdn == "Due") it.prdctdn = "Arriving"
                    else it.prdctdn = "in " + it.prdctdn + " minutes"
                    predictionList.add(it)
                }
            }
        }
        if (predictionList.size == 0) throw Exception("empty_list")
        else return predictionList
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

    //get walking directions from current location to a bus stop. google returns a ton of information
    //and you have to dig through the list to get what we want: a collection of lat/lon points
    //to draw a polyline on our map to represent walking directions
    suspend fun getDirectionsToStop(start: String, stop: String, key: String): List<LatLng> {
        val polylineCoords: MutableList<LatLng> = ArrayList()

        val directionResponse = mapsApiInterface.getNavigationToStop(
            start, stop, "walking", key)

        //too many damn lists
        val stepList = directionResponse.routeList?.get(0)?.tripList?.get(0)?.stepList

        stepList?.forEach {
            polylineCoords.add(it.endCoords!!.returnCoords())
        }

        return polylineCoords
    }
}