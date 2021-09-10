package com.taitsmith.busboy.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.taitsmith.busboy.obj.BusRoute;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopPredictionResponse;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivityViewModel extends AndroidViewModel {
    public String stopId, rt, apikey;
    public double lat, lon;
    public MutableLiveData<List<Prediction>> mutableLivePrediction;
    public MutableLiveData<String> errorMessage;

    MutableLiveData<List<BusRoute>> mutableLiveBusRoutes;
    List<Prediction> predictionList;
    ApiInterface apiInterface;
    SimpleLocation simpleLocation;

    public MainActivityViewModel(Application application) {
        super(application);
        simpleLocation = new SimpleLocation(application.getApplicationContext());
        mutableLivePrediction = new MutableLiveData<>();
        mutableLiveBusRoutes = new MutableLiveData<>();
        predictionList = new ArrayList<>();
        errorMessage = new MutableLiveData<>();
        apikey = "B344E43EEA2120C5CDDE8E5360D5928F"; //TODO MOVE THIS
    }

    public void getMutableLivePrediction() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<StopPredictionResponse> call = apiInterface.getStopPredictionList(stopId, rt, apikey);
        call.enqueue(new Callback<StopPredictionResponse>() {
            @Override
            public void onResponse(Call<StopPredictionResponse> call, Response<StopPredictionResponse> response) {
                if (response.body() == null || response.body().getBustimeResponse() == null) {
                    errorMessage.setValue("NULL_PRED_RESPONSE");
                } else {
                    predictionList.addAll(response.body().getBustimeResponse().getPrd());
                    mutableLivePrediction.setValue(predictionList);
                }
            }

            @Override
            public void onFailure(Call<StopPredictionResponse> call, Throwable t) {
                Log.d("BUS LIST FAILURE", t.getMessage());
            }
        });
    }

    public void getRoutes() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<BusRoute>> call = apiInterface.getRoutes(apikey);
        call.enqueue(new Callback<List<BusRoute>>() {
            @Override
            public void onResponse(Call<List<BusRoute>> call, Response<List<BusRoute>> response) {
                for (BusRoute br : response.body()) {
                    //DO SOMETHING HERE
                    Log.d("BUS ROUTE RESPONSE: ", br.getName()
                    .concat(" " + br.getRouteId())
                    .concat(" " + br.getDescription()));
                }
            }

            @Override
            public void onFailure(Call<List<BusRoute>> call, Throwable t) {
                Log.d("BUS ROUTE FAILURE", t.getMessage());
            }
        });
    }

    public void getNearbyStops() {
        simpleLocation = new SimpleLocation(getApplication().getApplicationContext());
        lat = (double) Math.round(simpleLocation.getLatitude() * 1000d) / 1000d;
        lon = (double) Math.round(simpleLocation.getLongitude() * 1000d) / 1000d;

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Stop>> call = apiInterface.getNearbyStops(lat, lon, apikey);
        call.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                if (response.body() == null) errorMessage.setValue("NO_NEARBY");
                else {
                    Log.d("NEARBY", response.body().toString());
                    for (Stop s : response.body()) {
                        Log.d("STOP NEARBY: ", s.getName());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Stop>> call, Throwable t) {
                Log.d("NEARBY ERROR", t.getMessage());
            }
        });
    }

    public void checkLocationPerm() {
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

    public void getRouteDirs(String name) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<String>> call = apiInterface.getRouteDirections(name, apikey);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
            }
        });
    }
}
