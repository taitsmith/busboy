package com.taitsmith.busboy.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.ui.MainActivity.Companion.mutableBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ByIdViewModel @Inject constructor(@AcTransitApiInterface
                                        private val acTransitApiInterface: ApiInterface,
                                        private val databaseRepository: DatabaseRepository,
                                        private val apiRepository: ApiRepository
) : ViewModel() {

    private val _stop = MutableLiveData<Stop>()
    val stop: LiveData<Stop> = _stop

    private val _stopPredictions = MutableLiveData<List<Prediction>>()
    val stopPredictions: LiveData<List<Prediction>> = _stopPredictions

    private val _bus = MutableLiveData<Bus>()
    val bus: LiveData<Bus> = _bus

    fun getStopPredictions(stopId: String, rt: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _stopPredictions.postValue(apiRepository.getStopPredictions(stopId, rt))
        }
    }

    fun getBusDetails(vid: String) {
        viewModelScope.launch {
            val call = acTransitApiInterface.getDetailedVehicleInfo(vid)
            call.enqueue(object: Callback<List<Bus>> {
                override fun onResponse(call: Call<List<Bus>>, response: Response<List<Bus>>) {
                    _bus.value = response.body()!![0]
                }

                override fun onFailure(call: Call<List<Bus>>, t: Throwable) {
                    Log.d("BUS DETAIL FAILURE: ", t.message!!)
                }
            })
        }
    }

    fun addStopToFavorites() {
        if (_stop.value == null) MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        else {
            viewModelScope.launch(Dispatchers.IO) {
                _stop.value?.linesServed = apiRepository.getLinesServed(_stop.value!!.stopId)
                databaseRepository.addStops(stop.value!!)
                MainActivityViewModel.mutableStatusMessage.postValue("FAVORITE_ADDED")
            }
        }
    }
    companion object {
        lateinit var mutableLinesServed: MutableLiveData<String>
    }

    init {
        mutableLinesServed = MutableLiveData()
    }
}