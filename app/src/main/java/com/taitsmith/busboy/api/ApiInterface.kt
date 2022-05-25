package com.taitsmith.busboy.api

import com.taitsmith.busboy.data.*
import com.taitsmith.busboy.ui.MainActivity
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    //returns predictions for given route at stop, otherwise all routes if rt == null
    @GET("actrealtime/prediction/")
    fun getStopPredictionList(
        @Query("stpid") stopId: String,
        @Query("rt") routeId: String?,
        @Query("token") token: String = MainActivity.acTransitApiKey
    ): Call<StopPredictionResponse>?

    //find all active stops within {distance} feet of point
    @GET("stops/{latitude}/{longitude}/")
    fun getNearbyStops(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("distance") distance: Int,
        @Query("active") active: Boolean,
        @Query("routeName") routeName: String?,
        @Query("token") token: String = MainActivity.acTransitApiKey
    ): Call<List<Stop?>?>?

    //get destinations for given stop so we can display NB/SB/EB/WB
    @GET("stop/{stopID}/destinations")
    fun getStopDestinations(
        @Path("stopID") stopId: String?,
        @Query("token") token: String = MainActivity.acTransitApiKey
    ): Call<StopDestinationResponse?>?

    //get lat/lon waypoints so we can draw the route on a map
    @GET("route/{route}/waypoints")
     fun getRouteWaypoints(
        @Path("route") route: String,
        @Query("token") token: String = MainActivity.acTransitApiKey
    ): Call<List<WaypointResponse?>?>?

    //get info about a bus so we can put it on the map
    @GET("vehicle/{vehicleId}")
     fun getVehicleInfo(
        @Path("vehicleId") vehicleId: String,
        @Query("token") token: String = MainActivity.acTransitApiKey
    ): Call<Bus?>?

    //talk to google and get walking directions from our location to the selected stop
    @GET("maps/api/directions/json")
     fun getNavigationToStop(
        @Query(value = "origin", encoded = true) origin: String,
        @Query(value = "destination", encoded = true) destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String
    ): Call<DirectionResponse?>?
}