package com.taitsmith.busboy.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction
import com.taitsmith.busboy.utils.ApiInterface
import com.taitsmith.busboy.utils.ApiClient
import com.taitsmith.busboy.obj.StopPredictionResponse
import com.taitsmith.busboy.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.util.ArrayList

class ByIdViewModel : ViewModel() {
    lateinit var mutableStopPredictions: MutableLiveData<List<Prediction>>
    private val apiInterface: ApiInterface? = ApiClient.getAcTransitClient().create(ApiInterface::class.java)
    var rt: String? = null

    fun getStopPredictions(stopId: String?) {
        if (rt == null) rt = ""
        viewModelScope.launch(Dispatchers.IO) {
            val call = apiInterface?.getStopPredictionList(stopId, rt, MainActivity.acTransitApiKey)
            call!!.enqueue(object : Callback<StopPredictionResponse?> {
                override fun onResponse(
                    call: Call<StopPredictionResponse?>,
                    response: Response<StopPredictionResponse?>
                ) {
                    if (response.body() == null || response.code() == 404) {
                        MainActivityViewModel.mutableErrorMessage.value = "NULL_PRED_RESPONSE"
                    } else {
                        predictionList.clear()
                        try {
                            for (pred in response.body()!!.bustimeResponse.prd) {
                                if (pred.dyn == 0) { //non-zero dyn means cancelled or not stopping
                                    predictionList.add(pred)
                                }
                            }
                        } catch (e: Exception) {
                            MainActivityViewModel.mutableErrorMessage.value = "NULL_PRED_RESPONSE"
                        }
                        if (predictionList.size == 0) MainActivityViewModel.mutableErrorMessage.value= "BAD INPUT"
                        else {
                            mutableStopPredictions.value = predictionList
                            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
                        }
                    }
                }

                override fun onFailure(call: Call<StopPredictionResponse?>, t: Throwable) {
                    Log.d("BUS LIST FAILURE", t.message!!)
                }
            })
        }
    }

    companion object {
        lateinit var predictionList: MutableList<Prediction>
    }
}