package com.taitsmith.busboy.viewmodels

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.taitsmith.busboy.utils.ApiInterface
import com.taitsmith.busboy.ui.MainActivity
import com.taitsmith.busboy.obj.StopDestinationResponse
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.obj.Stop
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(application: Application,
                                          @AcTransitApiInterface private val acTransitApiInterface: ApiInterface
                                          ) : AndroidViewModel(application) {

    lateinit var mutableNearbyStops: MutableLiveData<List<Stop?>>
    var rt: String? = null
    var distance: Int

    fun getNearbyStops() {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        if (rt == null) rt = ""
        viewModelScope.launch(Dispatchers.IO) {
            val call = acTransitApiInterface.getNearbyStops(
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
                    if (response.body() == null || response.code() == 404)
                        mutableErrorMessage.setValue("404")
                    else {
                        stopList.addAll(response.body()!!)
                        getLinesServed(stopList)
                    }
                }

                override fun onFailure(call: Call<List<Stop?>?>, t: Throwable) {
                    Log.d("NEARBY ERROR", t.message!!)
                    mutableErrorMessage.value = "CALL_FAILURE"
                }
            })
        }
    }

    fun getLinesServed(stopList: List<Stop?>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (s in stopList) {
                MainActivity.mutableNearbyStatusUpdater.postValue(s!!.name)
                val call = acTransitApiInterface.getStopDestinations(s.stopId, MainActivity.acTransitApiKey)
                try {
                    val response: Response<StopDestinationResponse?> = call!!.execute()
                    if (response.isSuccessful) {
                        val sb = StringBuilder()
                        val destinations = response.body()?.routeDestinations!!
                        destinations.forEach {
                            sb.append(it.routeId)
                                .append(" ")
                                .append(it.destination)
                                .append("\n")
                            s.linesServed = sb.toString()
                        }
                    } else {
                        mutableErrorMessage.postValue("404")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mutableErrorMessage.postValue("CALL_FAILURE")
                }
            }
            mutableNearbyStops.postValue(stopList)
        }
    }

    fun checkLocationPerm() {
        if (ContextCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!loc.hasLocationEnabled()) {
                mutableErrorMessage.value = "NO_LOC_ENABLED" //granted permissions, but location is disabled.
            } else {
                loc.beginUpdates()
                MainActivity.enableNearbySearch = true
            }
        } else {
            mutableErrorMessage.value = "NO_PERMISSION" //permissions not granted, so ask for them
        }
    }

    companion object {
        lateinit var stopList: MutableList<Stop?>
        lateinit var loc: SimpleLocation
    }

    init {
        loc = SimpleLocation(application.applicationContext)
        mutableNearbyStops = MutableLiveData()
        stopList = ArrayList()
        distance = 1000
    }
}