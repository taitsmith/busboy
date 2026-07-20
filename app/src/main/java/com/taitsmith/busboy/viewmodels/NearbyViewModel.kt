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
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.ApiRepository
import com.taitsmith.busboy.di.LocationRepository
import com.taitsmith.busboy.di.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val application: Application,
    private val apiRepository: ApiRepository,
    private val statusRepository: StatusRepository,
    private val locationRepository: LocationRepository
) : AndroidViewModel(application) {

    private val _isUpdated = MutableLiveData<Boolean>()
    val isUpdated: LiveData<Boolean> = _isUpdated

    //for getting lat/lon coordinates to draw walking directions on a map
    private val _directionPolylineCoords = MutableLiveData<List<LatLng>>()
    val directionPolylineCoords: LiveData<List<LatLng>> = _directionPolylineCoords

    private val _nearbyStopsFlow = MutableStateFlow<NearbyStopsState>(NearbyStopsState.Loading(ListLoadingState.START, emptyList()))
    val nearbyStopsState: StateFlow<NearbyStopsState> = _nearbyStopsFlow

    //only enable the search button if there's a location or users has selected 'choose on map'
    private val _enableSearchButton = MutableStateFlow(false)
    val enableSearchButton: StateFlow<Boolean> = _enableSearchButton

    var rt: String? = null
    var distance: Int

    /* some app functionality is different if we're using the device location or letting users
    pick a location on the map- we don't want to show location access dialogs if we aren't
    accessing the user's location, etc.
     */
    var isUsingLocation: Boolean = false

    //we only want to show the location choice method dialog once per session
    var shouldShowDialog = true

    /**
        The way AC Transit's API works, we have to make two calls to display everything on the
        'nearby' screen. One to find all nearby stops, and one to get the list of lines served.
        Then we can smoosh everything into one string with a \n between each to display it. So
        that's whats going on here and in the following method
        https://api.actransit.org/transit/Help/Api/GET-stop-stopId-destinations
     **/
    //gets a list of all stops within [distance] feet of [lat]/[lon] that serve line [rt]
    //or all stops if unspecified
    fun getNearbyStops() {
        statusRepository.isLoading(true)
        if (rt == null) rt = ""
        if (currentLocation.latitude == 0.0) {
            statusRepository.updateStatus("NULL_LOCATION")
        } else {
            viewModelScope.launch {
                val nearby = apiRepository.getNearbyStops(
                    LatLng(currentLocation.latitude, currentLocation.longitude),
                    distance,
                    rt
                )
                nearby.catch {
                    it.printStackTrace()
                    if (it.message.equals("timeout")) statusRepository.updateStatus("CALL_FAILURE")
                    else statusRepository.updateStatus("404")
                }
                .collect{
                    _nearbyStopsFlow.value = NearbyStopsState.Loading(ListLoadingState.PARTIAL, it)
                }
            }
        }
    }

    //collects edited stops (lines added) and rebuilds the list. Some providers (AC Transit) omit
    //stops that serve no lines, so the enriched stream can be shorter than the input list. Accumulate
    //whatever is emitted, in order, rather than indexing back into the original list by emission count
    //(which misaligned lines onto the wrong stop when any were skipped), and signal COMPLETE when the
    //stream finishes instead of counting to a fixed size (which never matched when stops were skipped).
    fun getNearbyStopsWithLines(stops: List<Stop>) {
        statusRepository.isLoading(false)
        viewModelScope.launch {
            val enrichedStops = mutableListOf<Stop>()
            apiRepository.getLinesServedByStops(stops).collect {
                enrichedStops.add(it)
                _nearbyStopsFlow.value = NearbyStopsState.Success(it)
            }
            _nearbyStopsFlow.value = NearbyStopsState.Loading(ListLoadingState.COMPLETE, enrichedStops)
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
                locationRepository.startUpdates()
                statusRepository.updateStatus("WAITING_ON_LOCATION")
                true
            } else {
                statusRepository.updateStatus("NO_LOC_ENABLED")
                false
            }
        }
        statusRepository.updateStatus("NO_PERMISSION")
        return false
    }

    //hey siri how do i walk from where i am to the bus stop
    fun getDirectionsToStop(start: String, stop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val coords = apiRepository.getDirectionsToStop(start, stop)
                //an empty route means Google returned no walkable steps- don't navigate to the map
                //(setupForRouteDisplay indexes coords[0]); surface it as a failure instead.
                if (coords.isEmpty()) {
                    statusRepository.updateStatus("DIRECTION_FAILURE")
                } else {
                    _directionPolylineCoords.postValue(coords)
                    _isUpdated.postValue(false)
                }
            }.onFailure {
                Log.d("FAILURE: ", it.message.toString())
                statusRepository.updateStatus("DIRECTION_FAILURE")
            }
        }
    }

    fun setIsUpdated(isUpdated: Boolean) {
        _isUpdated.value = isUpdated
    }

    fun setLocation(location: Location) {
        currentLocation = location
        _enableSearchButton.value = true
    }

    fun setIsUsingLocation(usingLocation: Boolean) {
        //if user has selected the option to use the device location, make sure we have
        //permission and location setting is enabled
        if (usingLocation && checkLocationPerm()) _enableSearchButton.value = true

        //disable the 'choose location method' dialog for now
        shouldShowDialog = false

        isUsingLocation = usingLocation
    }

    //return the screen to its initial, empty state. clearing the static currentLocation is the
    //root-cause fix for 'stuck with a location until the app is killed'- getNearbyStops() guards on
    //latitude == 0.0. the fragment re-opens the location-choice dialog so a new location can be picked.
    fun reset() {
        currentLocation = Location("provider")
        _enableSearchButton.value = false
        _nearbyStopsFlow.value = NearbyStopsState.Loading(ListLoadingState.START, emptyList())
        isUsingLocation = false
        rt = null
        distance = 1000
    }

    fun updateStatus(s: String) = statusRepository.updateStatus(s)

    private fun listenForLocation() {
        viewModelScope.launch {
            locationRepository.lastLocation.collect { location ->
                if (location != null) setLocation(location)
            }
        }
    }

    companion object {
        lateinit var loc: SimpleLocation
        lateinit var currentLocation: Location

        val locationPermGranted = MutableLiveData<Boolean>()
    }

    init {
        currentLocation = Location("provider")
        distance = 1000

        listenForLocation()
    }

    sealed class NearbyStopsState {
        data class Success(val stops: Stop): NearbyStopsState()
        data class Loading(val loadState: ListLoadingState, var stopList: List<Stop>): NearbyStopsState()
    }

    enum class ListLoadingState {
        START, PARTIAL, COMPLETE
    }
}