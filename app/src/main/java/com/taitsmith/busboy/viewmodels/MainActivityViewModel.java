package com.taitsmith.busboy.viewmodels;

import android.app.Application;
import android.util.Log;


import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.taitsmith.busboy.obj.DirectionResponseData;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityViewModel extends AndroidViewModel {
    public static MutableLiveData<String> mutableStatusMessage;
    public static MutableLiveData<String> mutableErrorMessage;
    public static List<LatLng> polylineCoords;

    ApiInterface apiInterface;

    public MainActivityViewModel(Application application) {
        super(application);
        mutableStatusMessage = new MutableLiveData<>();
        mutableErrorMessage = new MutableLiveData<>();

        polylineCoords = new ArrayList<>();
    }

    public void getDirectionsToStop(String start, String stop) {
        apiInterface = ApiClient.getMapsClient().create(ApiInterface.class);
        Call<DirectionResponseData> call = apiInterface.getNavigationToStop(start,
                stop,
                "walking",
                "AIzaSyCSMBC8N4Pnr59CoM2BwR7xOG665YBfr4A"); //TODO move me
        call.enqueue(new Callback<DirectionResponseData>() {
            @Override
            public void onResponse(Call<DirectionResponseData> call, Response<DirectionResponseData> response) {
                List<DirectionResponseData.Step> stepList = response.body().getRouteList().get(0)
                        .getTripList().get(0).getStepList();
                polylineCoords.clear();

                for (DirectionResponseData.Step step : stepList) {
                    polylineCoords.add(step.getEndCoords().returnCoords());
                }
                mutableStatusMessage.setValue("POLYLINE_READY");
            }

            @Override
            public void onFailure(Call<DirectionResponseData> call, Throwable t) {
                Log.d("DIRECTIONS FAILURE: ", t.getMessage());
            }
        });
    }
}
