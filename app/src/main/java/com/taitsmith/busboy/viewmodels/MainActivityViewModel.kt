package com.taitsmith.busboy.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.DirectionResponse
import com.taitsmith.busboy.api.WaypointResponse
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.R
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

@HiltViewModel
class MainActivityViewModel @Inject constructor(application: Application,
                                                @AcTransitApiInterface private val acTransitApiInterface: ApiInterface,
                                                @MapsApiInterface private val mapsApiInterface: ApiInterface,
                                                private val apiRepository: ApiRepository) : AndroidViewModel(application) {

    private val directionsApiKey: String

    fun getDirectionsToStop(start: String, stop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = mapsApiInterface.getNavigationToStop(
                start,
                stop,
                "walking",
                directionsApiKey
            )
            call!!.enqueue(object : Callback<DirectionResponse?>{
                override fun onResponse(
                    call: Call<DirectionResponse?>,
                    response: Response<DirectionResponse?>
                ) {
                    val stepList = response.body()!!
                        .routeList?.get(0)
                        ?.tripList?.get(0)?.stepList
                    polylineCoords.clear()
                    stepList!!.forEach { polylineCoords.add(it.endCoords!!.returnCoords()) }
                    mutableStatusMessage.value = "DIRECTION_POLYLINE_READY"
                }

                override fun onFailure(call: Call<DirectionResponse?>, t: Throwable) {
                    Log.d("DIRECTIONS FAILURE: ", t.message!!)
                    mutableErrorMessage.value = "CALL_FAILURE"
                }
            })
        }
    }

    fun getWaypoints(routeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = acTransitApiInterface.getRouteWaypoints(routeName)
            call!!.enqueue(object : Callback<List<WaypointResponse?>?> {
                override fun onResponse(
                    call: Call<List<WaypointResponse?>?>,
                    response: Response<List<WaypointResponse?>?>
                ) {
                    if (response.body() != null) {
                        val waypointList = response.body()!![0]?.patterns?.get(0)?.waypoints
                        polylineCoords.clear()
                        waypointList!!.forEach { polylineCoords.add(it.latLng) }
                        mutableStatusMessage.value = "ROUTE_POLYLINE_READY"
                    }
                }

                override fun onFailure(call: Call<List<WaypointResponse?>?>, t: Throwable) {
                    Log.d("waypoint failure ", t.message!!)
                    mutableErrorMessage.value = "CALL_FAILURE"
                }
            })
        }
    }

    companion object {
        lateinit var mutableStatusMessage: MutableLiveData<String>
        lateinit var mutableErrorMessage: MutableLiveData<String>
        lateinit var polylineCoords: MutableList<LatLng>
    }

    init {
        mutableStatusMessage = MutableLiveData()
        mutableErrorMessage = MutableLiveData()
        polylineCoords = ArrayList()
        directionsApiKey = application.getString(R.string.google_directions_key)
    }
}