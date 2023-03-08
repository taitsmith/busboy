package com.taitsmith.busboy.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    private val _stopPredictions = MutableLiveData<List<Prediction>>()
    val stopPredictions: LiveData<List<Prediction>> = _stopPredictions

    private val _busRouteWaypoints = MutableLiveData<List<LatLng>>()
    val busRouteWaypoints: LiveData<List<LatLng>> = _busRouteWaypoints

    private val _bus = MutableLiveData<Bus>()
    val bus: LiveData<Bus> = _bus

    fun getStopPredictions(stopId: String, rt: String?) {
        _stop.postValue(Stop(id = stopId.toLong(), stopId = stopId))

        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                _stopId.postValue(stopId)
                _stopPredictions.postValue(apiRepository.getStopPredictions(stopId, rt))
            }.onFailure {
                it.printStackTrace()
                Log.d("PREDICTIONS FAILURES: ", it.message.toString())
                when(it.message) {
                    "no_data" -> MainActivityViewModel.mutableErrorMessage.postValue("404")
                    "no_service"
                        -> MainActivityViewModel.mutableErrorMessage.postValue("CALL_FAILURE")
                    "empty_list" -> MainActivityViewModel.mutableErrorMessage.postValue("NULL_PRED_RESPONSE")
                    "timeout" -> MainActivityViewModel.mutableErrorMessage.postValue("CALL_FAILURE")
                }
            }
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch {
            _bus.value = apiRepository.getDetailedBusInfo(vid)
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
                    "empty_response" -> Log.d("GET WAYPOINTS", it.cause.toString())
                }
            }
        }
    }

    fun setIsUpdated(update: Boolean) {
        _isUpdated.value = update
    }
}