package com.taitsmith.busboy.viewmodels;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.taitsmith.busboy.obj.StopPredictionResponse;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import static com.taitsmith.busboy.ui.MainActivity.acTransitApiKey;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

public class ByIdViewModel extends ViewModel {
    public MutableLiveData<List<Prediction>> mutableStopPredictions;
    public static List<Prediction> predictionList;
    ApiInterface apiInterface;
    String rt;

    public ByIdViewModel() {
        mutableStopPredictions = new MutableLiveData<>();
        predictionList = new ArrayList<>();
    }

    public void getStopPredictions(@Nullable String stopId) {
        if (rt == null) rt = "";

        apiInterface = ApiClient.getAcTransitClient().create(ApiInterface.class);

        Call<StopPredictionResponse> call = apiInterface.getStopPredictionList(stopId, rt, acTransitApiKey);
        call.enqueue(new Callback<StopPredictionResponse>() {
            @Override
            public void onResponse(Call<StopPredictionResponse> call, Response<StopPredictionResponse> response) {

                if (response.body() == null || response.code() == 404) {
                    mutableErrorMessage.setValue("NULL_PRED_RESPONSE");
                } else {
                    predictionList.clear();
                    try {
                        predictionList.addAll(response.body().getBustimeResponse().getPrd());
                    } catch (NullPointerException | IndexOutOfBoundsException e) {
                        mutableErrorMessage.setValue("NULL_PRED_RESPONSE");
                    }
                    if (predictionList.size() == 0) mutableErrorMessage.setValue("BAD_INPUT");
                    else {
                        mutableStopPredictions.setValue(predictionList);
                        mutableStatusMessage.setValue("LOADED");
                    }
                }
            }

            @Override
            public void onFailure(Call<StopPredictionResponse> call, Throwable t) {
                Log.d("BUS LIST FAILURE", t.getMessage());
            }
        });
    }
}