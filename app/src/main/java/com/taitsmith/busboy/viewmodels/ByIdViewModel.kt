package com.taitsmith.busboy.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.api.RemoteDataSource
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
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
                                        private val apiRepository: ApiRepository
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
        RemoteDataSource.setStopInfo(id, rt)
        viewModelScope.launch {
            _stopId.postValue(id)
            apiRepository.stopPredictions
                .catch { exception ->
                    _predictionFlow.value = PredictionState.Error(exception)
                    when(exception.message) {
                    "no_data"       -> MainActivityViewModel.mutableErrorMessage.postValue("404")
                    "no_service"    -> MainActivityViewModel.mutableErrorMessage.postValue("NO_SERVICE_SCHEDULED")
                    "empty_list"    -> MainActivityViewModel.mutableErrorMessage.postValue("NULL_PRED_RESPONSE")
                    "timeout"       -> MainActivityViewModel.mutableErrorMessage.postValue("CALL_FAILURE")
                    }
                }
                .collect { preds ->
                _predictionFlow.value = PredictionState.Success(preds)
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
        viewModelScope.launch(Dispatchers.IO){
            kotlin.runCatching {
                _bus.postValue(apiRepository.getBusLocation(vehicleId))
            }.onFailure {
                when (it.message) {
                    "null_coords" -> MainActivityViewModel.mutableErrorMessage
                        .postValue("NULL_BUS_COORDS")
                }
            }
        }
    }

    fun addStopToFavorites() {
        if (_stop.value == null) MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        else {
            viewModelScope.launch(Dispatchers.IO) {
                stop.value?.linesServed = //oh lawd, we'll simplify this
                    apiRepository.getLinesServedByStop(listOf(stop.value!!))[0].linesServed
                databaseRepository.addStops(stop.value!!)
                MainActivityViewModel.mutableStatusMessage.postValue("FAVORITE_ADDED")
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
                    "empty_response" -> MainActivityViewModel.mutableErrorMessage.postValue("NO_WAYPOINTS")
                }
            }
        }
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