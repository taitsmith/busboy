package com.taitsmith.busboy.utils;

import com.taitsmith.busboy.obj.BusRoute;
import com.taitsmith.busboy.obj.StopPredictionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    //returns predictions for given route at stop, otherwise all routes if rt == null
    @GET("actrealtime/prediction/")
    Call<StopPredictionResponse> getStopPredictionList(@Query("stpid") String stopId,
                                                       @Query("rt") String routeId,
                                                       @Query("token") String token);

    @GET("routes")//list all AC Transit routes
    Call<List<BusRoute>> getRoutes(@Query("token") String token);

    @GET("route/{routeName}/directions")
    Call<List<String>> getRouteDirections(@Path("routeName") String routeName,
                                          @Query("token") String token);

}
