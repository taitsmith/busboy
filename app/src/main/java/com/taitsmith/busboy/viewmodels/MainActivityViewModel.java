package com.taitsmith.busboy.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.taitsmith.busboy.R;
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination;
import com.taitsmith.busboy.obj.DirectionResponseData;
import com.taitsmith.busboy.obj.StopDestinationResponse;
import com.taitsmith.busboy.ui.MainActivity;
import com.taitsmith.busboy.obj.Stop;
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
    public String apikey, rt;
    public MutableLiveData<List<Prediction>> mutableStopPredictions;
    public static MutableLiveData<String> mutableStatusMessage;
    public static MutableLiveData<String> mutableErrorMessage;
    public static List<LatLng> polylineCoords;
    public List<Prediction> predictionList;
    public int distance;
    public Location loc;

    ApiInterface apiInterface;
    SimpleLocation simpleLocation;
    FusedLocationProviderClient fusedLocation;

    public MainActivityViewModel(Application application) {
        super(application);
        mutableStopPredictions = new MutableLiveData<>();
        mutableStatusMessage = new MutableLiveData<>();
        mutableErrorMessage = new MutableLiveData<>();

        polylineCoords = new ArrayList<>();

        simpleLocation = new SimpleLocation(application.getApplicationContext());
        fusedLocation = new FusedLocationProviderClient(getApplication().getApplicationContext());

        predictionList = new ArrayList<>();

        apikey = application.getString(R.string.api_token);
        distance = 2000; //default distance in feet for nearby stops
        rt = ""; //route is optional in the 'nearby stops' call
    }

    public void getRoutesServed(Stop stop) {
        apiInterface = ApiClient.getAcTransitClient().create(ApiInterface.class);
        Call<List<RouteDestination>> call = apiInterface.getStopDirection(stop.getStopId(),
                apikey);
        call.enqueue(new Callback<List<StopDestinationResponse.RouteDestination>>() {
            @Override
            public void onResponse(Call<List<RouteDestination>> call, Response<List<RouteDestination>> response) {
                for (RouteDestination r : response.body()) {
                    Log.d("ROUTES SERVED ", r.destination);
                }
            }

            @Override
            public void onFailure(Call<List<StopDestinationResponse.RouteDestination>> call, Throwable t) {

            }
        });
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

    public void checkLocationPerm() {
        simpleLocation = new SimpleLocation(getApplication().getApplicationContext());
        if (ContextCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!simpleLocation.hasLocationEnabled()) {
                mutableStatusMessage.setValue("NO_LOC_ENABLED");
            } else MainActivity.getLocation(fusedLocation);

        } else {
            mutableStatusMessage.postValue("NO_PERMISSION");
        }
    }
}
