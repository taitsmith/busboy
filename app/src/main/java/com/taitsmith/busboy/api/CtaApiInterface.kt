package com.taitsmith.busboy.api

import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CTA "BusTracker" (Clever Devices BusTime) v3 endpoints. The API key and format=json are appended
 * to every request by an OkHttp interceptor (see ApiInterfaceModule), so they are not parameters here.
 * Predictions reuse [StopPredictionResponse]/[BustimeResponse] because CTA returns the same
 * bustime-response -> prd[] shape as AC Transit.
 */
interface CtaApiInterface {

    //predictions for a stop, optionally filtered by route
    @GET("getpredictions")
    suspend fun getPredictions(
        @Query("stpid") stopId: String,
        @Query("rt") routeId: String?
    ): ApiResult<StopPredictionResponse, Unit>

    //live vehicle location(s) by vehicle id
    @GET("getvehicles")
    suspend fun getVehicles(
        @Query("vid") vehicleId: String
    ): ApiResult<CtaVehicleResponse, Unit>

    //geo-positional pattern points for a route, used to draw the route polyline
    @GET("getpatterns")
    suspend fun getPatterns(
        @Query("rt") route: String
    ): ApiResult<CtaPatternResponse, Unit>

    //active detours, optionally filtered by route
    @GET("getdetours")
    suspend fun getDetours(
        @Query("rt") route: String?
    ): ApiResult<CtaDetourResponse, Unit>
}
