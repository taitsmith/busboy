package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.di.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ByIdViewModel @Inject constructor(
                                        private val databaseRepository: DatabaseRepository,
                                        private val apiRepository: ApiRepository,
                                        private val statusRepository: StatusRepository
) : ViewModel() {
    private val _isUpdated = MutableLiveData<Boolean>()
    var isUpdated: LiveData<Boolean> = _isUpdated

    private val _stopId = MutableLiveData<String>()
    val stopId: LiveData<String> = _stopId

    private val _stop = MutableLiveData<Stop>()
    val stop: LiveData<Stop> = _stop

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

    fun getPredictions(id: String, rt: String?) {
        statusRepository.isLoading(true)
        viewModelScope.launch {
            _stopId.postValue(id)
            val predictions = apiRepository.stopPredictions(id, rt)
            predictions
                .catch { exception ->
                    _predictions.value = PredictionState.Error(exception)
                }
                .collect { p ->
                    _stop.postValue(
                        Stop(
                            stopId = id,
                            name = p[0].stpnm
                        )
                    )
                    _predictions.value = PredictionState.Success(p)
                    getAlerts()
                    statusRepository.isLoading(false)
            }
        }
    }

    private fun getAlerts() {
        viewModelScope.launch {
            val alerts = apiRepository.serviceAlerts(stopId.value!!)
            alerts.collect {
                _alerts.postValue(it)
            }
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _bus.value = BusState.Detail(apiRepository.getDetailedBusInfo(vid))
            _isUpdated.postValue(false)
        }
    }

    fun getBusLocation(vehicleId: String, route: String) {
        statusRepository.isLoading(true)
        _bus.value = BusState.Loading
        this.route = route
        viewModelScope.launch {
            val b = apiRepository.vehicleInfo(vehicleId)
            b.catch { exception ->
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
                val stop = apiRepository.getLinesServedByStops(listOf(_stop.value!!))
                stop.collect {
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
                it.printStackTrace()
                when (it.message) {
                    "empty_response" -> statusRepository.updateStatus("NO_WAYPOINTS")
                }
            }
        }
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