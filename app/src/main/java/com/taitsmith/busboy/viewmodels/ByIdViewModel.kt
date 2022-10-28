package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.api.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ByIdViewModel @Inject constructor(@AcTransitApiInterface
                                        private val databaseRepository: DatabaseRepository,
                                        private val apiRepository: ApiRepository
) : ViewModel() {
    private val _stopId = MutableLiveData<String>()
    val stopId: LiveData<String> = _stopId

    private val _stop = MutableLiveData<Stop>()
    val stop: LiveData<Stop> = _stop

    private val _stopPredictions = MutableLiveData<List<Prediction>>()
    val stopPredictions: LiveData<List<Prediction>> = _stopPredictions

    private val _bus = MutableLiveData<Bus>()
    val bus: LiveData<Bus> = _bus

    fun getStopPredictions(stopId: String, rt: String?) {
        _stop.postValue(Stop(id = stopId.toLong(), stopId = stopId))

        viewModelScope.launch(Dispatchers.IO) {
            _stopId.postValue(stopId)
            _stopPredictions.postValue(apiRepository.getStopPredictions(stopId, rt))
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch {
            _bus.value = apiRepository.getDetailedBusInfo(vid)
        }
    }

    fun getBusLocation(vehicleId: String) {
        viewModelScope.launch(Dispatchers.IO){
            _bus.postValue(apiRepository.getBusLocation(vehicleId))
        }
    }

    fun addStopToFavorites() {
        if (_stop.value == null) MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        else {
            viewModelScope.launch(Dispatchers.IO) {
                _stop.value?.linesServed = apiRepository.getLinesServed(_stop.value!!.stopId!!)
                databaseRepository.addStops(stop.value!!)
                MainActivityViewModel.mutableStatusMessage.postValue("FAVORITE_ADDED")
            }
        }
    }
}