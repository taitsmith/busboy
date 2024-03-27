package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.AcTransitRemoteDataSource
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.utils.StatusRepo
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
                                        private val statusRepo: StatusRepo
) : ViewModel() {
    private val _isUpdated = MutableLiveData<Boolean>()
    var isUpdated: LiveData<Boolean> = _isUpdated

    private val _stopId = MutableLiveData<String>()
    val stopId: LiveData<String> = _stopId

    private val _stop = MutableLiveData<Stop>()
    val stop: LiveData<Stop> = _stop

    private val _busRouteWaypoints = MutableLiveData<List<LatLng>>()
    val busRouteWaypoints: LiveData<List<LatLng>> = _busRouteWaypoints

    private val _bus = MutableLiveData<Bus>()
    val bus: LiveData<Bus> = _bus

    private val _alerts = MutableLiveData<ServiceAlertResponse>()
    val alerts: LiveData<ServiceAlertResponse> = _alerts

    private val _alertShown = MutableLiveData(false)
    val alertShown: LiveData<Boolean> = _alertShown

    private val _predictionFlow = MutableStateFlow<PredictionState>(PredictionState.Loading(false))
    val predictionFlow: StateFlow<PredictionState> = _predictionFlow

    fun getPredictions(id: String, rt: String?) {
        statusRepo.isLoading(true)
        AcTransitRemoteDataSource.setStopInfo(id, rt)
        viewModelScope.launch {
            _stopId.postValue(id)
            apiRepository.stopPredictions
                .catch { exception ->
                    _predictionFlow.value = PredictionState.Error(exception)
                }
                .collect { predictions ->
                    _predictionFlow.value = PredictionState.Success(predictions)
                    getAlerts()
                    statusRepo.isLoading(false)
            }
        }
    }

    private fun getAlerts() {
        viewModelScope.launch {
            apiRepository.serviceAlerts.collect {
                _alerts.postValue(it)
            }
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _bus.postValue(apiRepository.getDetailedBusInfo(vid))
            _isUpdated.postValue(false)
        }
    }

    fun getBusLocation(vehicleId: String) {
        statusRepo.isLoading(true)
        viewModelScope.launch(Dispatchers.IO){
            kotlin.runCatching {
                _bus.postValue(apiRepository.getBusLocation(vehicleId))
            }.onFailure {
                when (it.message) {
                    "null_coords" -> statusRepo.updateStatus("NULL_BUS_COORDS")
                }
            }
        }
    }

    fun addStopToFavorites() {
        if (_stop.value == null) statusRepo.updateStatus("BAD_INPUT")
        else {
            viewModelScope.launch(Dispatchers.IO) {
                stop.value?.linesServed = //oh lawd, we'll simplify this
                    apiRepository.getLinesServedByStop(listOf(stop.value!!))[0].linesServed
                databaseRepository.addStops(stop.value!!)
                statusRepo.updateStatus("FAVORITE_ADDED")
            }
        }
    }

    fun getWaypoints(routeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                _busRouteWaypoints.postValue(apiRepository.getBusRouteWaypoints(routeName))
                _isUpdated.postValue(false)
            }.onFailure {
                it.printStackTrace()
                when (it.message) {
                    "empty_response" -> statusRepo.updateStatus("NO_WAYPOINTS")
                }
            }
        }
    }

    fun updateStatus(loading: Boolean?, message: String?) {
        if (loading == null) statusRepo.updateStatus(message!!)
        else statusRepo.isLoading(loading)
    }

    fun setIsUpdated(update: Boolean) {
        _isUpdated.value = update
    }

    fun setAlertShown(shown: Boolean) {
        _alertShown.value = shown
    }

    sealed class PredictionState {
        data class Success(val predictions: List<Prediction>): PredictionState()
        data class Error(val exception: Throwable): PredictionState()
        data class Loading(val loading: Boolean): PredictionState()
    }
}