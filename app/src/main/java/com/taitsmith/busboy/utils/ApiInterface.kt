package com.taitsmith.busboy.utils

import com.taitsmith.busboy.obj.*
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    //returns predictions for given route at stop, otherwise all routes if rt == null
    @GET("actrealtime/prediction/")
    fun getStopPredictionList(
        @Query("stpid") stopId: String?,
        @Query("rt") routeId: String?,
        @Query("token") token: String?
    ): Call<StopPredictionResponse?>?

    @GET("route/{routeName}/directions")
    fun getRouteDirections(
        @Path("routeName") routeName: String?,
        @Query("token") token: String?
    ): Call<List<String?>?>?

    //find all active stops within {distance} feet of point
    @GET("stops/{latitude}/{longitude}/")
    fun getNearbyStops(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("distance") distance: Int,
        @Query("active") active: Boolean,
        @Query("routeName") routeName: String?,
        @Query("token") token: String?
    ): Call<List<Stop?>?>?

    @GET("stop/{stopID}/destinations")
    fun  //get destinations for given stop so we can display NB/SB/EB/WB
            getStopDestinations(
        @Path("stopID") stopId: String?,
        @Query("token") token: String?
    ): Call<StopDestinationResponse?>?

    @GET("route/{route}/waypoints")
     fun  //get lat/lon waypoints so we can draw the route on a map
            getRouteWaypoints(
        @Path("route") route: String?,
        @Query("token") token: String?
    ): Call<List<WaypointResponse?>?>?

    @GET("vehicle/{vehicleId}")
     fun  //get info about a bus so we can put it on the map
            getVehicleInfo(
        @Path("vehicleId") vehicleId: String?,
        @Query("token") token: String?
    ): Call<Bus?>?

    @GET("maps/api/directions/json")
     fun  //talk to google and get walking directions from our location to the selected stop
            getNavigationToStop(
        @Query(value = "origin", encoded = true) origin: String?,
        @Query(value = "destination", encoded = true) destination: String?,
        @Query("mode") mode: String?,
        @Query("key") apiKey: String?
    ): Call<DirectionResponseData?>?
}