package com.taitsmith.busboy.viewmodels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopDestinationResponse;
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.taitsmith.busboy.ui.MainActivity.acTransitApiKey;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

public class NearbyViewModel extends AndroidViewModel {
    public MutableLiveData<List<Stop>> mutableNearbyStops;
    public static MutableLiveData<HashMap<String, String>> mutableHashMap;
    public static MutableLiveData<SimpleLocation> mutableSimpleLocation;
    public static List<Stop> stopList;
    public static SimpleLocation loc;

    ApiInterface apiInterface;
    String rt;
    int distance;

    public NearbyViewModel(Application application) {
        super(application);
        loc = new SimpleLocation(application.getApplicationContext());
        mutableSimpleLocation = new MutableLiveData<>();
        mutableNearbyStops = new MutableLiveData<>();
        mutableHashMap = new MutableLiveData<>();
        stopList = new ArrayList<>();
        distance = 2000;
    }

    @SuppressLint("MissingPermission") //won't end up here without permissions
    public void getNearbyStops() {
        if (rt == null) rt = "";

        apiInterface = ApiClient.getAcTransitClient().create(ApiInterface.class);
        Call<List<Stop>> call = apiInterface.getNearbyStops(
                loc.getLatitude(),
                loc.getLongitude(),
                750,
                true,
                rt,
                acTransitApiKey);
        call.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                if (response.body() == null || response.code() == 404)
                    mutableErrorMessage.setValue("NEARBY_404");
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

    public void getDestinationHashMap(List<String> stopList) {
        HashMap<String, String> destinationHashMap = new HashMap<>(stopList.size());
        apiInterface = ApiClient.getAcTransitClient().create(ApiInterface.class);
        for (String s : stopList) {
            Call<StopDestinationResponse> call = apiInterface.getStopDestinations(s, acTransitApiKey);
            call.enqueue(new Callback<StopDestinationResponse>() {
                @Override
                public void onResponse(Call<StopDestinationResponse> call, Response<StopDestinationResponse> response) {
                    StringBuilder sb = new StringBuilder();
                    for (RouteDestination d : response.body().routeDestinations) {
                        sb.append(d.routeId)
                                .append(" ")
                                .append(d.destination)
                                .append("\n");
                        Log.d("HASHMAP RESPONSE: ", sb.toString());
                        destinationHashMap.put(s, sb.toString());
                        mutableHashMap.setValue(destinationHashMap);
                    }
                }

                @Override
                public void onFailure(Call<StopDestinationResponse> call, Throwable t) {
                    Log.d("DESTINATION FAILURE: ", t.getMessage());
                }
            });
        }
    }

    public void checkLocationPerm() {
        if (ContextCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!loc.hasLocationEnabled()) {
                mutableErrorMessage.setValue("NO_LOC_ENABLED"); //granted permissions, but location is disabled.
            } else {
                mutableSimpleLocation.setValue(loc);
                mutableStatusMessage.setValue("LOADING");
                loc.beginUpdates();
                loc.setListener(this::getNearbyStops);
            }

        } else {
            mutableErrorMessage.setValue("NO_PERMISSION"); //permissions not granted, so ask for them
        }
    }
}