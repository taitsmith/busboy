package com.taitsmith.busboy.viewmodels;

import static com.taitsmith.busboy.ui.MainActivity.acTransitApiKey;
import static com.taitsmith.busboy.ui.MainActivity.mutableBus;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.taitsmith.busboy.R;
import com.taitsmith.busboy.di.AcTransitRetrofit;
import com.taitsmith.busboy.di.MapsRetrofit;
import com.taitsmith.busboy.obj.Bus;
import com.taitsmith.busboy.obj.DirectionResponseData;
import com.taitsmith.busboy.obj.WaypointResponse;
import com.taitsmith.busboy.di.MapsRetrofitModule;
import com.taitsmith.busboy.utils.ApiInterface;
import com.taitsmith.busboy.obj.WaypointResponse.Pattern.Waypoint;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@HiltViewModel
public class MainActivityViewModel extends AndroidViewModel {
    public static MutableLiveData<String> mutableStatusMessage;
    public static MutableLiveData<String> mutableErrorMessage;
    public static List<LatLng> polylineCoords;

    @AcTransitRetrofit
    @Inject
    Retrofit acTransitRetrofit;
    @MapsRetrofit
    @Inject
    Retrofit mapsRetrofit;

    ApiInterface googleApiInterface, acTransitApiInterface;
    String directionsApiKey;

    public MainActivityViewModel(Application application) {
        super(application);
        mutableStatusMessage = new MutableLiveData<>();
        mutableErrorMessage = new MutableLiveData<>();

        polylineCoords = new ArrayList<>();
        acTransitApiInterface = acTransitRetrofit.create(ApiInterface.class);
        googleApiInterface = mapsRetrofit.create(ApiInterface.class);
        directionsApiKey = application.getString(R.string.google_directions_key);
    }

    public void getDirectionsToStop(String start, String stop) {
        Call<DirectionResponseData> call = googleApiInterface.getNavigationToStop(start,
                stop,
                "walking",
                directionsApiKey);
        call.enqueue(new Callback<DirectionResponseData>() {
            @Override
            public void onResponse(Call<DirectionResponseData> call, Response<DirectionResponseData> response) {
                List<DirectionResponseData.Step> stepList = response.body().getRouteList().get(0)
                        .getTripList().get(0).getStepList();
                polylineCoords.clear();

                for (DirectionResponseData.Step step : stepList) {
                    polylineCoords.add(step.getEndCoords().returnCoords());
                }
                mutableStatusMessage.setValue("DIRECTION_POLYLINE_READY");
            }

            @Override
            public void onFailure(Call<DirectionResponseData> call, Throwable t) {
                Log.d("DIRECTIONS FAILURE: ", t.getMessage());
            }
        });
    }

    public void getWaypoints(String routeName){
        Call<List<WaypointResponse>> call = acTransitApiInterface.getRouteWaypoints(routeName, acTransitApiKey);
        call.enqueue(new Callback<List<WaypointResponse>>() {
            @Override
            public void onResponse(Call<List<WaypointResponse>> call, Response<List<WaypointResponse>> response) {
                if (response.body() != null) {
                    List<Waypoint> waypointList = response.body().get(0).getPatterns().get(0).getWaypoints();
                    polylineCoords.clear();
                    for (Waypoint wp : waypointList) {
                       polylineCoords.add(new LatLng(wp.getLatitude(), wp.getLongitude()));
                    }
                    mutableStatusMessage.setValue("ROUTE_POLYLINE_READY");
                }
            }

            @Override
            public void onFailure(Call<List<WaypointResponse>> call, Throwable t) {
                Log.d("waypoint failure ", t.getMessage());
            }
        });
    }

    public void getBusLocation(String vehicleId) {
        Call<Bus> call = acTransitApiInterface.getVehicleInfo(vehicleId, acTransitApiKey);
        call.enqueue(new Callback<Bus>() {
            @Override
            public void onResponse(Call<Bus> call, Response<Bus> response) {
                if (response.code() == 404) mutableErrorMessage.setValue("404");
                if (response.body() != null ) {
                    mutableBus.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<Bus> call, Throwable t) {
                mutableErrorMessage.setValue("404");
            }
        });
    }
}
