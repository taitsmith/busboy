package com.taitsmith.busboy.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.taitsmith.busboy.obj.StopPredictionResponse;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityViewModel extends ViewModel {
    MutableLiveData<List<Prediction>> mutableLivePrediction;
    public String lat, lon, stopId, rt, apikey;
    List<Prediction> predictionList;
    ApiInterface apiInterface;

    public MainActivityViewModel() {
        mutableLivePrediction = new MutableLiveData<>();
        predictionList = new ArrayList<>();
        rt = null;
        stopId = null;
        apikey = "B344E43EEA2120C5CDDE8E5360D5928F"; //TODO MOVE THIS
    }

    public MutableLiveData<List<Prediction>> getMutableLivePrediction() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<StopPredictionResponse> call = apiInterface.getStopPredictionList(stopId, rt, apikey);
        call.enqueue(new Callback<StopPredictionResponse>() {
            @Override
            public void onResponse(Call<StopPredictionResponse> call, Response<StopPredictionResponse> response) {

                for (Prediction p : response.body().getBustimeResponse().getPrd()) {
                    Log.d("PREDICTION LIST ", p.getDes());
                    predictionList.add(p);
                }
                mutableLivePrediction.setValue(predictionList);
            }

            @Override
            public void onFailure(Call<StopPredictionResponse> call, Throwable t) {
                Log.d("BUS LIST FAILURE", t.getMessage());
            }
        });

        return mutableLivePrediction;
    }
}
