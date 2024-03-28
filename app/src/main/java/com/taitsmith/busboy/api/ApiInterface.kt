package com.taitsmith.busboy.api

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import com.taitsmith.busboy.BuildConfig
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    //returns predictions for given route at stop, otherwise all routes if rt == null
    @DecodeErrorBody
    @GET("actrealtime/prediction/")
    suspend fun getStopPredictionList(
        @Query("stpid") stopId: String,
        @Query("rt") routeId: String?,
        @Query("token") token: String = BuildConfig.ac_transit_key
    ): ApiResult<StopPredictionResponse, Unit>

    //find all active stops within {distance} feet of point
    @GET("stops/{latitude}/{longitude}/")
    suspend fun getNearbyStops(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("distance") distance: Int,
        @Query("active") active: Boolean,
        @Query("routeName") routeName: String?,
        @Query("token") token: String = BuildConfig.ac_transit_key
    ): ApiResult<List<Stop>, Unit>

    //get destinations for given stop so we can display NB/SB/EB/WB
    @GET("stop/{stopID}/destinations")
    suspend fun getStopDestinations(
        @Path("stopID") stopId: String?,
        @Query("token") token: String = BuildConfig.ac_transit_key
    ): ApiResult<StopDestinationResponse, Unit>

    //get lat/lon waypoints so we can draw the route on a map
    @GET("route/{route}/waypoints")
    suspend fun getBusRouteWaypoints(
        @Path("route") route: String,
        @Query("token") token: String = BuildConfig.ac_transit_key
    ): List<WaypointResponse>

    //get info about a bus so we can put it on the map
    @GET("vehicle/{vehicleId}")
     suspend fun getVehicleInfo(
        @Path("vehicleId") vehicleId: String,
        @Query("token") token: String = BuildConfig.ac_transit_key
    ): Bus

    //get detailed info about a bus because you're a nerd and you like that stuff
     @GET("vehicle/{vehicleId}/characteristics")
     suspend fun getDetailedVehicleInfo(
        @Path("vehicleId") vehicleId: String,
        @Query("token") token: String = BuildConfig.ac_transit_key
     ): List<Bus>

     //get alerts for lines served by selected stop
     @GET("actrealtime/servicebulletin")
     suspend fun getServiceAlertsForStop(
         @Query("stpid") stopId: String,
         @Query("token") token: String = BuildConfig.ac_transit_key
     ): ApiResult<ServiceAlertResponse, Unit>

    //talk to google and get walking directions from our location to the selected stop
    @GET("maps/api/directions/json")
     suspend fun getNavigationToStop(
        @Query(value = "origin", encoded = true) origin: String,
        @Query(value = "destination", encoded = true) destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String = BuildConfig.google_directions_key
    ): DirectionResponse
}