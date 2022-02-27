package com.taitsmith.busboy.utils;

import com.google.android.gms.maps.model.LatLng;
import com.taitsmith.busboy.obj.BusRoute;
import com.taitsmith.busboy.obj.DirectionResponseData;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination;
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

    //TODO delete these two, do we actually use them?
    @GET("routes")//list all AC Transit routes
    Call<List<BusRoute>> getRoutes(@Query("token") String token);

    @GET("route/{routeName}/directions")
    Call<List<String>> getRouteDirections(@Path("routeName") String routeName,
                                          @Query("token") String token);

    //find all active stops within {distance} feet of point
    @GET("stops/{latitude}/{longitude}/")
    Call<List<Stop>> getNearbyStops(@Path("latitude") double latitude,
                                    @Path("longitude") double longitude,
                                    @Query("distance") int distance,
                                    @Query("active") boolean active,
                                    @Query("routeName") String routeName,
                                    @Query("token") String token);

    @GET("stop/{stopID}/destinations")//get destinations for given stop so we can display NB/SB/EB/WB
    Call<List<RouteDestination>> getStopDirection(@Path("stopID") String stopId,
                                                  @Query("token") String token);


    @GET("maps/api/directions/json") //talk to google and get walking directions from our location to the selected stop
    Call<DirectionResponseData> getNavigationToStop(@Query(value = "origin", encoded = true) String origin,
                                                    @Query(value = "destination", encoded = true) String destination,
                                                    @Query("mode") String mode,
                                                    @Query("key") String apiKey);

}
