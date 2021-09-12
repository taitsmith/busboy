package com.taitsmith.busboy.viewmodels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopPredictionResponse;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivityViewModel extends AndroidViewModel {
    public String stopId, rt, apikey;
    public MutableLiveData<List<Prediction>> mutableStopPredictions;
    public MutableLiveData<List<Stop>> mutableNearbyStops;
    public MutableLiveData<String> errorMessage;
    public List<Stop> stopList;
    public List<Prediction> predictionList;

    ApiInterface apiInterface;
    SimpleLocation simpleLocation;

    public MainActivityViewModel(Application application) {
        super(application);
        mutableStopPredictions = new MutableLiveData<>();
        mutableNearbyStops = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();

        stopList = new ArrayList<>();
        predictionList = new ArrayList<>();

        apikey = "B344E43EEA2120C5CDDE8E5360D5928F"; //TODO MOVE THIS
    }

    public void getStopPredictions() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<StopPredictionResponse> call = apiInterface.getStopPredictionList(stopId, rt, apikey);
        call.enqueue(new Callback<StopPredictionResponse>() {
            @Override
            public void onResponse(Call<StopPredictionResponse> call, Response<StopPredictionResponse> response) {

                if (response.body() == null || response.code() == 404) {
                    errorMessage.setValue("NULL_PRED_RESPONSE");
                } else {
                    predictionList.clear();
                    predictionList.addAll(response.body().getBustimeResponse().getPrd());
                    mutableStopPredictions.setValue(predictionList);
                }
            }

            @Override
            public void onFailure(Call<StopPredictionResponse> call, Throwable t) {
                Log.d("BUS LIST FAILURE", t.getMessage());
            }
        });
    }

    public void getNearbyStops() {
        //simpleLocation doesn't seem to return the correct emulator location, so we have to do it old school
        LocationManager lm = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") //we can't get here without permission
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Stop>> call = apiInterface.getNearbyStops(loc.getLatitude(), loc.getLongitude(), apikey);
        call.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                if (response.body() == null || response.code() == 404)
                    errorMessage.setValue("NEARBY_404");
                else {
                    stopList.clear();
                    stopList.addAll(response.body());
                    mutableNearbyStops.setValue(stopList);
                    }
                }

            @Override
            public void onFailure(Call<List<Stop>> call, Throwable t) {
                Log.d("NEARBY ERROR", t.getMessage());
            }
        });
    }

    public void checkLocationPerm() {
        simpleLocation = new SimpleLocation(getApplication().getApplicationContext());
        if (ContextCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!simpleLocation.hasLocationEnabled()) {
                errorMessage.setValue("NO_LOC_ENABLED");
            }
            getNearbyStops();
        } else {
            errorMessage.postValue("NO_PERMISSION");
        }
    }
}
