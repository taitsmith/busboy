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

    private val _locationEnabled = MutableLiveData<Boolean>()
    var locationEnabled: LiveData<Boolean> = _locationEnabled

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
                    loc.longitude,
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

    //check to see if we've been given permission to access location. if we have, check
    //to see if location is enabled. if both are true, we can ask for a location, if not
    //we'll either prompt for location permission or to enable permission, depending on whats missing
    fun checkLocationPerm(): Boolean {
        if (!loc.hasLocationEnabled()) {
            mutableErrorMessage.value = "NO_LOC_ENABLED" //granted permissions, but location is disabled.
            _locationEnabled.value = false
            return false
        } else {
            if (ContextCompat.checkSelfPermission(
                    getApplication<Application>().applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermGranted.value = true
                loc.beginUpdates()
                _locationEnabled.value = true
            } else {
                mutableErrorMessage.value = "NO_PERMISSION"
                return false
            }
        }
        return true
    }


    //hey siri how do i walk from where i am to the bus stop
    fun getDirectionsToStop(start: String, stop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _directionPolylineCoords.postValue(apiRepository.getDirectionsToStop(start, stop, mapsKey))
        }
    }

    companion object {
        lateinit var loc: SimpleLocation

        val locationPermGranted = MutableLiveData<Boolean>()
    }

    init {
        loc = SimpleLocation(application.applicationContext)
        distance = 1000
    }
}