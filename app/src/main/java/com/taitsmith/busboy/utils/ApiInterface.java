package com.taitsmith.busboy.utils;

import com.taitsmith.busboy.obj.StopPredictionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    //returns predictions for given route at stop, otherwise all routes if rt == null
    @GET("actrealtime/prediction/")
    Call<StopPredictionResponse> getStopPredictionList(@Query("stpid") String stopId,
                                                       @Query("rt") String routeId,
                                                       @Query("token") String token);

}
