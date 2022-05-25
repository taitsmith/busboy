package com.taitsmith.busboy.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.api.StopPredictionResponse
import com.taitsmith.busboy.api.StopPredictionResponse.BustimeResponse.Prediction
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.di.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ByIdViewModel @Inject constructor(@AcTransitApiInterface
                                        private val acTransitApiInterface: ApiInterface,
                                        private val databaseRepository: DatabaseRepository,
                                        private val apiRepository: ApiRepository)
                                        : ViewModel() {

    var rt: String? = null

    var stop: Stop? = null
    var mutableStopPredictions: MutableLiveData<List<Prediction>>

    fun getStopPredictions(stopId: String) {
        if (rt == null) rt = ""

        stop = Stop(id = stopId.toLong(), stopId = stopId)

        viewModelScope.launch(Dispatchers.IO) {
            val call = acTransitApiInterface.getStopPredictionList(stopId, rt)
            call!!.enqueue(object : Callback<StopPredictionResponse> {
                override fun onResponse(
                    call: Call<StopPredictionResponse>,
                    response: Response<StopPredictionResponse>
                ) {
                    if (response.body() == null || response.code() == 404) {
                        MainActivityViewModel.mutableErrorMessage.value = "NULL_PRED_RESPONSE"
                    } else {
                        predictionList.clear()
                        try {
                            for (p in response.body()!!.bustimeResponse?.prd!!) {
                                if (p.dyn == 0) { //non-zero dyn means cancelled or not stopping
                                    if (p.prdctdn == "1" || p.prdctdn == "Due") p.prdctdn = "Arriving"
                                    else p.prdctdn = "in " + p.prdctdn + " minutes"
                                    predictionList.add(p)
                                }
                            }
                        } catch (e: Exception) {
                            MainActivityViewModel.mutableErrorMessage.value = "NULL_PRED_RESPONSE"
                        }
                        if (predictionList.size == 0) MainActivityViewModel.mutableErrorMessage.value= "BAD_INPUT"
                        else {
                            mutableStopPredictions.value = predictionList
                            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
                        }
                    }
                }

                override fun onFailure(call: Call<StopPredictionResponse>, t: Throwable) {
                    Log.d("BUS LIST FAILURE", t.message!!)
                    MainActivityViewModel.mutableErrorMessage.value = "CALL_FAILURE"
                }
            })
        }
    }

    fun addStopToFavorites() {
        if (stop == null) MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        else {
            viewModelScope.launch(Dispatchers.IO) {
                stop!!.linesServed = apiRepository.getLinesServed(stop!!.stopId!!)
                databaseRepository.addStops(stop!!)
                MainActivityViewModel.mutableStatusMessage.postValue("FAVORITE_ADDED")
            }
        }
    }

    companion object {
        lateinit var predictionList: MutableList<Prediction>
        lateinit var mutableLinesServed: MutableLiveData<String>
    }

    init {
        predictionList = ArrayList()
        mutableStopPredictions = MutableLiveData()
        mutableLinesServed = MutableLiveData()
    }
}