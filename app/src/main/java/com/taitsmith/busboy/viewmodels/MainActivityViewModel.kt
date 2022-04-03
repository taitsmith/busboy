package com.taitsmith.busboy.viewmodels


import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject
import retrofit2.Retrofit
import com.taitsmith.busboy.di.MapsRetrofit
import com.taitsmith.busboy.utils.ApiInterface
import com.taitsmith.busboy.obj.DirectionResponseData
import com.taitsmith.busboy.obj.WaypointResponse
import com.taitsmith.busboy.ui.MainActivity
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.obj.Bus
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.R
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
                                                ) : AndroidViewModel(application) {

    @AcTransitApiInterface
    @Inject lateinit var acTransitApiInterface: ApiInterface
    @MapsApiInterface
    @Inject lateinit var mapsApiInterface: ApiInterface
    private var directionsApiKey: String

    fun getDirectionsToStop(start: String?, stop: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = mapsApiInterface.getNavigationToStop(
                start,
                stop,
                "walking",
                directionsApiKey
            )
            call!!.enqueue(object : Callback<DirectionResponseData?>{
                override fun onResponse(
                    call: Call<DirectionResponseData?>,
                    response: Response<DirectionResponseData?>
                ) {

                    val stepList = response.body()!!
                        .routeList[0]
                        .tripList[0].stepList
                    polylineCoords.clear()
                    for (step in stepList) {
                        polylineCoords.add(step.endCoords.returnCoords())
                    }
                    mutableStatusMessage.value = "DIRECTION_POLYLINE_READY"
                }

                override fun onFailure(call: Call<DirectionResponseData?>, t: Throwable) {
                    Log.d("DIRECTIONS FAILURE: ", t.message!!)
                    mutableErrorMessage.value = "CALL_FAILURE"
                }
            })
        }
    }

    fun getWaypoints(routeName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val call =
                acTransitApiInterface.getRouteWaypoints(routeName, MainActivity.acTransitApiKey)
            call!!.enqueue(object : Callback<List<WaypointResponse?>?> {
                override fun onResponse(
                    call: Call<List<WaypointResponse?>?>,
                    response: Response<List<WaypointResponse?>?>
                ) {
                    if (response.body() != null) {
                        val waypointList = response.body()!![0]?.patterns?.get(0)?.waypoints
                        polylineCoords.clear()
                        for (wp in waypointList!!) {
                            polylineCoords.add(LatLng(wp.latitude, wp.longitude))
                        }
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

    fun getBusLocation(vehicleId: String?) {
        viewModelScope.launch(Dispatchers.IO){
        val call = acTransitApiInterface.getVehicleInfo(vehicleId, MainActivity.acTransitApiKey)
        call!!.enqueue(object : Callback<Bus?> {
            override fun onResponse(call: Call<Bus?>, response: Response<Bus?>) {
                if (response.code() == 404) mutableErrorMessage.value = "404"
                if (response.body() != null) {
                    MainActivity.mutableBus.value = response.body()
                }
            }

            override fun onFailure(call: Call<Bus?>, t: Throwable) {
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