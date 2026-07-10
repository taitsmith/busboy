package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.slack.eithernet.ApiResult
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.DirectionResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.api.StopPredictionResponse
import com.taitsmith.busboy.api.WaypointResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Exercises [AcTransitRemoteDataSource] directly against a mocked [ApiInterface]. Unlike the ViewModel
 * and ApiRepository tests (which swap in FakeRemoteDataSource), these tests verify the real
 * EitherNet [ApiResult.Failure] -> string-code contract that everything downstream depends on:
 * ApiFailure/HttpFailure -> "404", NetworkFailure -> "CALL_FAILURE", UnknownFailure -> "UNKNOWN".
 */
@RunWith(JUnit4::class)
class AcTransitRemoteDataSourceTest {

    private lateinit var acTransit: ApiInterface
    private lateinit var maps: ApiInterface
    private lateinit var remoteDataSource: AcTransitRemoteDataSource

    @Before
    fun setup() {
        acTransit = mock()
        maps = mock()
        remoteDataSource = AcTransitRemoteDataSource(acTransit, maps)
    }

    // region predictions -------------------------------------------------------------------------

    @Test
    fun `predictions emits bustime response on success`() = runTest {
        val btr = BustimeResponse()
        whenever(acTransit.getStopPredictionList(any(), anyOrNull(), any()))
            .thenReturn(ApiResult.success(StopPredictionResponse(btr)))

        remoteDataSource.predictions("55555", null).first() shouldBe btr
    }

    @Test
    fun `predictions maps every eithernet failure to its status code`() = runTest {
        suspend fun errorFor(result: ApiResult<StopPredictionResponse, Unit>): String? {
            whenever(acTransit.getStopPredictionList(any(), anyOrNull(), any())).thenReturn(result)
            return try {
                remoteDataSource.predictions("x", null).first()
                null
            } catch (e: Exception) {
                e.message
            }
        }

        errorFor(ApiResult.apiFailure<Unit>()) shouldBe "404"
        errorFor(ApiResult.httpFailure<Unit>(404)) shouldBe "404"
        errorFor(ApiResult.networkFailure(IOException("no connection"))) shouldBe "CALL_FAILURE"
        errorFor(ApiResult.unknownFailure(RuntimeException("???"))) shouldBe "UNKNOWN"
    }

    // endregion

    // region vehicleLocation ---------------------------------------------------------------------

    @Test
    fun `vehicleLocation emits bus on success`() = runTest {
        val bus = Bus().apply { vehicleId = 1529 }
        whenever(acTransit.getVehicleInfo(any(), any())).thenReturn(ApiResult.success(bus))

        remoteDataSource.vehicleLocation("1529").first() shouldBe bus
    }

    @Test
    fun `vehicleLocation maps every eithernet failure to its status code`() = runTest {
        suspend fun errorFor(result: ApiResult<Bus, Unit>): String? {
            whenever(acTransit.getVehicleInfo(any(), any())).thenReturn(result)
            return try {
                remoteDataSource.vehicleLocation("x").first()
                null
            } catch (e: Exception) {
                e.message
            }
        }

        errorFor(ApiResult.apiFailure<Unit>()) shouldBe "404"
        errorFor(ApiResult.httpFailure<Unit>(500)) shouldBe "404"
        errorFor(ApiResult.networkFailure(IOException())) shouldBe "CALL_FAILURE"
        errorFor(ApiResult.unknownFailure(RuntimeException())) shouldBe "UNKNOWN"
    }

    // endregion

    // region serviceAlerts -----------------------------------------------------------------------

    @Test
    fun `serviceAlerts emits response on success`() = runTest {
        val response = ServiceAlertResponse(BustimeResponse())
        whenever(acTransit.getServiceAlertsForStop(any(), any())).thenReturn(ApiResult.success(response))

        remoteDataSource.serviceAlerts("55555").first() shouldBe response
    }

    @Test
    fun `serviceAlerts maps failures to status codes`() = runTest {
        suspend fun errorFor(result: ApiResult<ServiceAlertResponse, Unit>): String? {
            whenever(acTransit.getServiceAlertsForStop(any(), any())).thenReturn(result)
            return try {
                remoteDataSource.serviceAlerts("x").first()
                null
            } catch (e: Exception) {
                e.message
            }
        }

        errorFor(ApiResult.httpFailure<Unit>(404)) shouldBe "404"
        errorFor(ApiResult.networkFailure(IOException())) shouldBe "CALL_FAILURE"
        errorFor(ApiResult.unknownFailure(RuntimeException())) shouldBe "UNKNOWN"
    }

    // endregion

    // region nearbyStops -------------------------------------------------------------------------

    @Test
    fun `nearbyStops emits list on success`() = runTest {
        val stops = listOf(Stop().apply { stopId = "55555" })
        whenever(acTransit.getNearbyStops(any(), any(), any(), any(), anyOrNull(), any()))
            .thenReturn(ApiResult.success(stops))

        remoteDataSource.nearbyStops(LatLng(1.1, 1.1), 1000, null).first() shouldBe stops
    }

    @Test
    fun `nearbyStops maps failure to status code`() = runTest {
        whenever(acTransit.getNearbyStops(any(), any(), any(), any(), anyOrNull(), any()))
            .thenReturn(ApiResult.networkFailure(IOException()))

        val msg = try {
            remoteDataSource.nearbyStops(LatLng(1.1, 1.1), 1000, null).first()
            null
        } catch (e: Exception) {
            e.message
        }
        msg shouldBe "CALL_FAILURE"
    }

    // endregion

    // region linesServedByStop -------------------------------------------------------------------

    @Test
    fun `linesServedByStop builds a Stop with its lines from destinations`() = runTest {
        val response = StopDestinationResponse().apply {
            stopId = 55555
            routeDestinations = listOf(
                StopDestinationResponse.RouteDestination(routeId = "51A", destination = "Fruitvale BART")
            )
        }
        whenever(acTransit.getStopDestinations(anyOrNull(), any())).thenReturn(ApiResult.success(response))

        val emitted = remoteDataSource
            .linesServedByStop(listOf(Stop().apply { stopId = "55555"; name = "Broadway & 25th St" }))
            .first()

        emitted.name shouldBe "Broadway & 25th St"
        emitted.stopId shouldBe "55555"
        emitted.linesServed?.trim() shouldBe "51A Fruitvale BART"
    }

    @Test
    fun `linesServedByStop maps failure to status code`() = runTest {
        whenever(acTransit.getStopDestinations(anyOrNull(), any()))
            .thenReturn(ApiResult.apiFailure<Unit>())

        val msg = try {
            remoteDataSource.linesServedByStop(listOf(Stop().apply { stopId = "1" })).first()
            null
        } catch (e: Exception) {
            e.message
        }
        msg shouldBe "404"
    }

    // endregion

    // region getBusRouteWaypoints ----------------------------------------------------------------

    @Test
    fun `getBusRouteWaypoints drills through patterns to collect lat lng`() = runTest {
        val waypoint = mock<WaypointResponse.Pattern.Waypoint> {
            on { latLng } doReturn LatLng(1.1, 2.2)
        }
        val pattern = mock<WaypointResponse.Pattern> {
            on { waypoints } doReturn listOf(waypoint)
        }
        val response = mock<WaypointResponse> {
            on { patterns } doReturn listOf(pattern)
        }
        whenever(acTransit.getBusRouteWaypoints(any(), any())).thenReturn(listOf(response))

        remoteDataSource.getBusRouteWaypoints("51A") shouldBe listOf(LatLng(1.1, 2.2))
    }

    @Test
    fun `getBusRouteWaypoints throws empty_response when api returns nothing`() = runTest {
        whenever(acTransit.getBusRouteWaypoints(any(), any())).thenReturn(emptyList())

        val msg = try {
            remoteDataSource.getBusRouteWaypoints("51A")
            null
        } catch (e: Exception) {
            e.message
        }
        msg shouldBe "empty_response"
    }

    // endregion

    // region getDirectionsToStop -----------------------------------------------------------------

    @Test
    fun `getDirectionsToStop collects end coords from each step`() = runTest {
        val end = DirectionResponse.EndCoords().apply { lat = 1.1; lon = 2.2 }
        val step = DirectionResponse.Step().apply { endCoords = end }
        val leg = DirectionResponse.Leg().apply { stepList = listOf(step) }
        val route = DirectionResponse.MapRoute().apply { tripList = listOf(leg) }
        val directionResponse = DirectionResponse().apply { routeList = listOf(route) }
        whenever(maps.getNavigationToStop(any(), any(), any(), any())).thenReturn(directionResponse)

        remoteDataSource.getDirectionsToStop("origin", "destination") shouldBe listOf(LatLng(1.1, 2.2))
    }

    // endregion
}
