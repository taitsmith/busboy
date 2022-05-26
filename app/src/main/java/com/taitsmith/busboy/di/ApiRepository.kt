package com.taitsmith.busboy.di

import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class ApiRepository @Inject constructor(@AcTransitApiInterface
                                        val acTransitApiInterface: ApiInterface,
                                        @MapsApiInterface
                                        val mapsApiInterface: ApiInterface){
    /*
        The way AC Transit's API works, we have to make two calls to display everything on the
        'nearby' screen. One to find all nearby stops, and this one to get the list of lines served.
        Then we can smoosh everything into one string with a \n between each to display it.
        https://api.actransit.org/transit/Help/Api/GET-stop-stopId-destinations
     */
    fun getLinesServed(stopId: String): String {
        val sb = StringBuilder()
        val call = acTransitApiInterface.getStopDestinations(stopId)
        try {
            val response = call!!.execute()
            for (s in response.body()?.routeDestinations!!) {
                sb.append(s.routeId)
                        .append(" ")
                        .append(s.destination)
                        .append("\n")
            }
            return sb.toString()
        } catch (e: Exception) {
            MainActivityViewModel.mutableErrorMessage.postValue("404")
        }
        return sb.toString()
    }
}