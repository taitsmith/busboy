package com.taitsmith.busboy.viewmodels

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.R
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(application: Application,
                                          private val apiRepository: ApiRepository
                                          ) : AndroidViewModel(application) {

    private val mapsKey: String = application.getString(R.string.google_directions_key)

    private val _nearbyStops = MutableLiveData<List<Stop>>()
    val nearbyStops: LiveData<List<Stop>> = _nearbyStops

    private val _locationPermGranted = MutableLiveData<Boolean>()
    var locationPermGranted: LiveData<Boolean> = _locationPermGranted

    //for getting lat/lon coordinates to draw walking directions on a map
    private val _directionPolylineCoords = MutableLiveData<List<LatLng>>()
    val directionPolylineCoords: LiveData<List<LatLng>> = _directionPolylineCoords

    var rt: String? = null
    var distance: Int

    fun getNearbyStops() {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        if (rt == null) rt = ""
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val nearbyList = apiRepository.getNearbyStops(
                    loc.latitude,
                    loc.latitude,
                    distance,
                    true,
                    rt
                )
                _nearbyStops.postValue(apiRepository.getLinesServedByStop(nearbyList))
            }.onFailure {
                it.printStackTrace()
                mutableErrorMessage.postValue("404")
            }
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
                _locationPermGranted.value = false
            } else {
                loc.beginUpdates()
                _locationPermGranted.value = true
            }
        } else {
            mutableErrorMessage.value = "NO_PERMISSION" //permissions not granted, so ask for them
        }
    }

    //hey siri how do i walk from where i am to the bus stop
    fun getDirectionsToStop(start: String, stop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _directionPolylineCoords.postValue(apiRepository.getDirectionsToStop(start, stop, mapsKey))
        }
    }

    companion object {
        lateinit var loc: SimpleLocation
    }

    init {
        loc = SimpleLocation(application.applicationContext)
        distance = 1000
    }
}