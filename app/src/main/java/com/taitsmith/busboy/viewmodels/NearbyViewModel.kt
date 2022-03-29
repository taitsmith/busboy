package com.taitsmith.busboy.viewmodels

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.taitsmith.busboy.utils.ApiInterface
import com.taitsmith.busboy.utils.ApiClient
import com.taitsmith.busboy.ui.MainActivity
import com.taitsmith.busboy.obj.StopDestinationResponse
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.obj.Stop
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashMap

class NearbyViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var mutableNearbyStops: MutableLiveData<List<Stop?>>
    private val apiInterface: ApiInterface? = ApiClient.getAcTransitClient().create(ApiInterface::class.java)
    var rt: String? = null
    var distance: Int

    fun getNearbyStops() {
        if (rt == null) rt = ""
        viewModelScope.launch(Dispatchers.IO) {
            val call = apiInterface!!.getNearbyStops(
                loc.latitude,
                loc.longitude,
                distance,
                true,
                rt,
                MainActivity.acTransitApiKey
            )
            call!!.enqueue(object : Callback<List<Stop?>?> {
                override fun onResponse(
                    call: Call<List<Stop?>?>,
                    response: Response<List<Stop?>?>
                ) {
                    if (response.body() == null || response.code() == 404) MainActivityViewModel.mutableErrorMessage.setValue(
                        "404"
                    ) else {
                        stopList.clear()
                        stopList.addAll(response.body()!!)
                        mutableNearbyStops.setValue(stopList)
                    }
                }

                override fun onFailure(call: Call<List<Stop?>?>, t: Throwable) {
                    Log.d("NEARBY ERROR", t.message!!)
                    MainActivityViewModel.mutableErrorMessage.value = "CALL FAILURE"
                }
            })
        }
    }

    fun getDestinationHashMap(stopList: List<String>) {
        val destinationHashMap = HashMap<String, String>(stopList.size)
        viewModelScope.launch(Dispatchers.IO) {
            for (s in stopList) {
                val call = apiInterface!!.getStopDestinations(s, MainActivity.acTransitApiKey)
                call!!.enqueue(object : Callback<StopDestinationResponse?> {
                    override fun onResponse(
                        call: Call<StopDestinationResponse?>,
                        response: Response<StopDestinationResponse?>
                    ) {
                        val sb = StringBuilder()
                        val destinations = response.body()?.routeDestinations!!
                        for (d in destinations) {
                            sb.append(d.routeId)
                                .append(" ")
                                .append(d.destination)
                                .append("\n")
                            destinationHashMap[s] = sb.toString()
                            mutableHashMap.value = destinationHashMap
                        }
                    }

                    override fun onFailure(call: Call<StopDestinationResponse?>, t: Throwable) {
                        Log.d("GET DESTINATION ", "FAILURE")
                    }
                })
            }
        }
        MainActivityViewModel.mutableStatusMessage.value = "LOADED"
    }

    fun checkLocationPerm() {
        if (ContextCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!loc.hasLocationEnabled()) {
                MainActivityViewModel.mutableErrorMessage.value = "NO_LOC_ENABLED" //granted permissions, but location is disabled.
            } else {
                mutableSimpleLocation.value = loc
                MainActivityViewModel.mutableStatusMessage.value = "LOADING"
                loc.beginUpdates()
                loc.setListener { getNearbyStops() }
            }
        } else {
            MainActivityViewModel.mutableErrorMessage.value = "NO_PERMISSION" //permissions not granted, so ask for them
        }
    }

    companion object {
        lateinit var mutableHashMap: MutableLiveData<HashMap<String, String>>
        lateinit var mutableSimpleLocation: MutableLiveData<SimpleLocation>
        lateinit var stopList: MutableList<Stop?>
        lateinit var loc: SimpleLocation
    }

    init {
        loc = SimpleLocation(application.applicationContext)
        mutableSimpleLocation = MutableLiveData()
        mutableNearbyStops = MutableLiveData()
        mutableHashMap = MutableLiveData()
        stopList = ArrayList()
        distance = 2000
    }
}