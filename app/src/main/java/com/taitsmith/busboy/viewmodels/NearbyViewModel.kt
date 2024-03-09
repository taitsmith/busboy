package com.taitsmith.busboy.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val application: Application,
    private val apiRepository: ApiRepository,
                                          ) : AndroidViewModel(application) {

    private val _permGrantedAndEnabled = MutableLiveData<Boolean>()
    var permGrantedAndEnabled: LiveData<Boolean> = _permGrantedAndEnabled

    private val _isUpdated = MutableLiveData<Boolean>()
    val isUpdated: LiveData<Boolean> = _isUpdated

    private val _nearbyStops = MutableLiveData<List<Stop>>()
    val nearbyStops: LiveData<List<Stop>> = _nearbyStops

    //for getting lat/lon coordinates to draw walking directions on a map
    private val _directionPolylineCoords = MutableLiveData<List<LatLng>>()
    val directionPolylineCoords: LiveData<List<LatLng>> = _directionPolylineCoords

    var rt: String? = null
    var distance: Int

    /* some app functionality is different if we're using the device location or letting users
    pick a location on the map- we don't want to show location access dialogs if we aren't
    accessing the user's location, etc.
     */
    var isUsingLocation: Boolean = false

    //we only want to show the location choice method dialog once per session
    var shouldShowDialog = true

    fun getNearbyStops() {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        if (rt == null) rt = ""
        if (currentLocation.latitude == 0.0) {
            mutableErrorMessage.value = "NULL_LOCATION"
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val nearbyList = apiRepository.getNearbyStops(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        distance,
                        true,
                        rt
                    )
                    _nearbyStops.postValue(apiRepository.getLinesServedByStop(nearbyList))
                }.onFailure {
                    it.printStackTrace()
                    if (it.message == "timeout") mutableErrorMessage.postValue("CALL_FAILURE")
                    else mutableErrorMessage.postValue("404")
                }
            }
        }
    }

    //check to see if we've been given permission to access location. if we have, check
    //to see if location is enabled. if both are true, we can ask for a location, if not
    //we'll either prompt for location permission or to enable permission, depending on whats missing
    fun checkLocationPerm(): Boolean {
        loc = SimpleLocation(application.applicationContext)

        if (ContextCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return if (loc.hasLocationEnabled()) {
                _permGrantedAndEnabled.value = true
                true
            } else {
                mutableErrorMessage.value = "NO_LOC_ENABLED" //granted permissions, but location is disabled.
                false
            }
        }
        mutableErrorMessage.value = "NO_PERMISSION"
        return false
    }

    //hey siri how do i walk from where i am to the bus stop
    fun getDirectionsToStop(start: String, stop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                _directionPolylineCoords.postValue(apiRepository.getDirectionsToStop(start, stop))
                _isUpdated.postValue(false)
            }.onFailure {
                Log.d("FAILURE: ", it.message.toString())
                mutableErrorMessage.postValue("DIRECTION_FAILURE")
            }
        }
    }

    fun setIsUpdated(isUpdated: Boolean) {
        _isUpdated.value = isUpdated
    }

    fun setLocation(location: Location) {
        currentLocation = location
    }

    fun setIsUsingLocation(usingLocation: Boolean) {
        //if user has selected the option to use the device location, make sure we have
        //permission and location setting is enabled
        if (usingLocation && checkLocationPerm()) _permGrantedAndEnabled.value = true

        //disable the 'choose location method' dialog for now
        shouldShowDialog = false

        isUsingLocation = usingLocation
    }


    companion object {
        lateinit var loc: SimpleLocation
        lateinit var currentLocation: Location

        val locationPermGranted = MutableLiveData<Boolean>()
    }

    init {
        currentLocation = Location(null)
        distance = 1000
    }
}