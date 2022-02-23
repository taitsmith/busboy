package com.taitsmith.busboy.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.ui.MainActivity;
import com.taitsmith.busboy.utils.ApiClient;
import com.taitsmith.busboy.utils.ApiInterface;

import java.util.List;

import im.delight.android.location.SimpleLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearbyViewModel extends AndroidViewModel {
    public MutableLiveData<List<Stop>> mutableNearbyStops;
    public List<Stop> stopList;

    SimpleLocation loc;
    ApiInterface apiInterface;
    String rt, apikey;
    int distance;

    public NearbyViewModel(Application application) {
        super(application);
    }

    @SuppressLint("MissingPermission") //won't end up here without permissions
    public void getNearbyStops() {
        apiInterface = ApiClient.getAcTransitClient().create(ApiInterface.class);
        Call<List<Stop>> call = apiInterface.getNearbyStops(loc.getLatitude(),
                loc.getLongitude(),
                distance,
                rt,
                apikey);
        call.enqueue(new Callback<List<Stop>>() {
            @Override
            public void onResponse(Call<List<Stop>> call, Response<List<Stop>> response) {
                if (response.body() == null || response.code() == 404)
                    MainActivityViewModel.mutableStatusMessage.setValue("NEARBY_404");
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
}