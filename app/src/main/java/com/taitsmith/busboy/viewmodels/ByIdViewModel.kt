package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.ApiRepository
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.di.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class ByIdViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val apiRepository: ApiRepository,
    private val statusRepository: StatusRepository
) : ViewModel() {
    private val _isUpdated = MutableLiveData<Boolean>()
    var isUpdated: LiveData<Boolean> = _isUpdated

    private val _stopId = MutableLiveData<String?>()
    val stopId: LiveData<String?> = _stopId

    private val _stop = MutableLiveData<Stop?>()
    val stop: LiveData<Stop?> = _stop

    private val _busRouteWaypoints = MutableLiveData<List<LatLng>>()
    val busRouteWaypoints: LiveData<List<LatLng>> = _busRouteWaypoints

    private val _alerts = MutableLiveData<ServiceAlertResponse>()
    val alerts: LiveData<ServiceAlertResponse> = _alerts

    private val _alertShown = MutableLiveData(false)
    val alertShown: LiveData<Boolean> = _alertShown

    private val _bus = MutableStateFlow<BusState>(BusState.Loading)
    val bus: StateFlow<BusState> = _bus

    private val _predictions = MutableStateFlow<PredictionState>(PredictionState.Loading(false))
    val predictions: StateFlow<PredictionState> = _predictions

    private var route: String= ""

    //the prediction/vehicle flows refresh forever (60s loop); hold their jobs so a new search- or an
    //agency switch (via reset())- cancels the previous poller instead of stacking another on top.
    private var predictionsJob: Job? = null
    private var busLocationJob: Job? = null

    fun getPredictions(id: String, rt: String?) {
        statusRepository.isLoading(true)
        predictionsJob?.cancel()
        predictionsJob = viewModelScope.launch {
            _stopId.postValue(id)
            apiRepository.stopPredictions(id, rt)
                .catch { exception ->
                    statusRepository.isLoading(false)
                    _predictions.value = PredictionState.Error(exception)
                }
                .collect { p ->
                    //an empty-but-successful list is a valid stop with no (stopping) buses- e.g. every
                    //prediction was dropped by the dyn filter. don't index p[0]; that throws inside the
                    //collector where .catch can't see it, crashing the app.
                    val first = p.firstOrNull()
                    if (first == null) {
                        statusRepository.updateStatus("NULL_PRED_RESPONSE")
                        statusRepository.isLoading(false)
                        return@collect
                    }
                    _stop.postValue(
                        Stop(
                            stopId = id,
                            name = first.stpnm
                        )
                    )
                    _predictions.value = PredictionState.Success(p)
                    getAlerts(id)
                    statusRepository.isLoading(false)
                }
        }
    }

    @VisibleForTesting
    fun getAlerts() {
        val id = stopId.value ?: return
        getAlerts(id)
    }

    //alerts are best-effort context shown over the predictions- a fetch failure must not crash the app
    //or interrupt the prediction view, so swallow it. the id is passed in rather than read from the
    //async-posted stopId LiveData, which may not have landed yet when this runs.
    private fun getAlerts(id: String) {
        viewModelScope.launch {
            apiRepository.serviceAlerts(id)
                .catch { }
                .collect { _alerts.postValue(it) }
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bus.value = BusState.Detail(apiRepository.getDetailedBusInfo(vid))
                _isUpdated.postValue(false)
            } catch (e: Exception) {
                _bus.value = BusState.Error(e)
            }
        }
    }

    fun getBusLocation(vehicleId: String, route: String) {
        statusRepository.isLoading(true)
        _bus.value = BusState.Loading
        this.route = route
        busLocationJob?.cancel()
        busLocationJob = viewModelScope.launch {
            apiRepository.vehicleLocation(vehicleId)
                .catch { exception ->
                    statusRepository.isLoading(false)
                    _bus.value = BusState.Error(exception)
                }
                .collect {
                    if (bus.value == BusState.Loading) _bus.value = BusState.Initial(it)
                    else _bus.value = BusState.Updated(it)
                }
        }
    }

    fun addStopToFavorites() {
        if (_stop.value == null) statusRepository.updateStatus("BAD_INPUT")
        else {
            viewModelScope.launch(Dispatchers.IO) {
                apiRepository.getLinesServedByStops(listOf(_stop.value!!))
                    .catch { statusRepository.updateStatus(it.message ?: "UNKNOWN") }
                    .collect {
                        databaseRepository.addStops(it)
                        statusRepository.updateStatus("FAVORITE_ADDED")
                    }
            }
        }
    }

    fun getWaypoints() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                _busRouteWaypoints.postValue(apiRepository.getBusRouteWaypoints(route))
                _isUpdated.postValue(false)
            }.onFailure {
                //emit a status either way- MainActivity hides the progress bar on any status update,
                //so a waypoint failure can't leave the UI stuck in the loading state.
                when (it.message) {
                    "empty_response" -> statusRepository.updateStatus("NO_WAYPOINTS")
                    else -> statusRepository.updateStatus(it.message ?: "UNKNOWN")
                }
            }
        }
    }

    //return the screen to its initial, empty state. see NearbyViewModel.reset() for the sibling.
    //the predictions/bus collectors do nothing on Loading, so resetting to Loading won't re-render.
    //clearing _stopId matters- otherwise an empty-field search re-fetches the last stop (see the
    //fragment's search() fallback).
    fun reset() {
        predictionsJob?.cancel()
        busLocationJob?.cancel()
        _predictions.value = PredictionState.Loading(false)
        _bus.value = BusState.Loading
        _stopId.value = null
        _stop.value = null
        _alertShown.value = false
        _isUpdated.value = false
        route = ""
    }

    fun updateStatus(loading: Boolean?, message: String?) {
        if (loading == null) statusRepository.updateStatus(message!!)
        else statusRepository.isLoading(loading)
    }

    fun setIsUpdated(update: Boolean) {
        _isUpdated.value = update
    }

    fun setAlertShown(shown: Boolean) {
        _alertShown.value = shown
    }

    @VisibleForTesting
    fun setStopId(id: String) {
        _stopId.value = id
    }

    sealed class PredictionState {
        data class Success(val predictions: List<Prediction>):  PredictionState()
        data class Error(val exception: Throwable):             PredictionState()
        data class Loading(val loading: Boolean):               PredictionState()
    }

    sealed class BusState {
        data object Loading:                        BusState()
        data class Initial(val bus: Bus):           BusState()
        data class Updated(val bus: Bus):           BusState()
        data class Detail(val bus: Bus):            BusState()
        data class Error(val exception: Throwable): BusState()
    }
}