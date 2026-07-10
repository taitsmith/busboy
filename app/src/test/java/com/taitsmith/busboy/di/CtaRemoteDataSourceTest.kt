package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.slack.eithernet.ApiResult
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.CtaApiInterface
import com.taitsmith.busboy.api.CtaDetour
import com.taitsmith.busboy.api.CtaDetourResponse
import com.taitsmith.busboy.api.CtaPatternResponse
import com.taitsmith.busboy.api.CtaVehicle
import com.taitsmith.busboy.api.CtaVehicleResponse
import com.taitsmith.busboy.api.StopPredictionResponse
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Verifies [CtaRemoteDataSource] maps CTA BusTracker responses into the shared domain types and
 * honors the same EitherNet failure -> string-code contract as the AC Transit source.
 */
@RunWith(JUnit4::class)
class CtaRemoteDataSourceTest {

    private lateinit var cta: CtaApiInterface
    private lateinit var maps: ApiInterface
    private lateinit var dataSource: CtaRemoteDataSource

    @Before
    fun setup() {
        cta = mock()
        maps = mock()
        dataSource = CtaRemoteDataSource(cta, maps)
    }

    @Test
    fun `predictions emits bustime response on success`() = runTest {
        val btr = BustimeResponse()
        whenever(cta.getPredictions(any(), anyOrNull()))
            .thenReturn(ApiResult.success(StopPredictionResponse(btr)))

        dataSource.predictions("1926", null).first() shouldBe btr
    }

    @Test
    fun `predictions maps every eithernet failure to its status code`() = runTest {
        suspend fun errorFor(result: ApiResult<StopPredictionResponse, Unit>): String? {
            whenever(cta.getPredictions(any(), anyOrNull())).thenReturn(result)
            return try {
                dataSource.predictions("x", null).first(); null
            } catch (e: Exception) { e.message }
        }

        errorFor(ApiResult.apiFailure<Unit>()) shouldBe "404"
        errorFor(ApiResult.httpFailure<Unit>(404)) shouldBe "404"
        errorFor(ApiResult.networkFailure(IOException())) shouldBe "CALL_FAILURE"
        errorFor(ApiResult.unknownFailure(RuntimeException())) shouldBe "UNKNOWN"
    }

    @Test
    fun `vehicleLocation maps a CTA vehicle into a Bus`() = runTest {
        val ctaVehicle = CtaVehicle().apply {
            vid = "509"; lat = "41.921"; lon = "-87.648"; hdg = "358"; des = "Waveland/Broadway"
        }
        val body = CtaVehicleResponse.Body().apply { vehicle = listOf(ctaVehicle) }
        whenever(cta.getVehicles(any())).thenReturn(ApiResult.success(CtaVehicleResponse(body)))

        val bus = dataSource.vehicleLocation("509").first()
        bus.vehicleId shouldBe 509
        bus.latitude shouldBe 41.921
        bus.longitude shouldBe -87.648
        bus.heading shouldBe 358
        bus.description shouldBe "Waveland/Broadway"
    }

    @Test
    fun `serviceAlerts maps detours into the service-alert shape`() = runTest {
        val detour = CtaDetour().apply {
            desc = "IVD MultiRoute detour 47"; st = 1
            rtdirs = listOf(CtaDetour.RtDir().apply { rt = "72"; dir = "NORTHBOUND" })
        }
        val body = CtaDetourResponse.Body().apply { dtrs = listOf(detour) }
        whenever(cta.getDetours(anyOrNull())).thenReturn(ApiResult.success(CtaDetourResponse(body)))

        val alerts = dataSource.serviceAlerts("1926").first().bustimeResponse.sb!!
        alerts.size shouldBe 1
        alerts[0].nm shouldBe "IVD MultiRoute detour 47"
        alerts[0].srvc[0].rt shouldBe "72"
    }

    @Test
    fun `getBusRouteWaypoints picks the longest pattern ordered by seq`() = runTest {
        val short = CtaPatternResponse.Pattern().apply {
            pt = listOf(CtaPatternResponse.Point().apply { seq = 1; lat = 9.0; lon = 9.0 })
        }
        val long = CtaPatternResponse.Pattern().apply {
            pt = listOf(
                CtaPatternResponse.Point().apply { seq = 3; lat = 3.0; lon = 3.0 },
                CtaPatternResponse.Point().apply { seq = 1; lat = 1.0; lon = 1.0 },
                CtaPatternResponse.Point().apply { seq = 2; lat = 2.0; lon = 2.0 }
            )
        }
        val body = CtaPatternResponse.Body().apply { ptr = listOf(short, long) }
        whenever(cta.getPatterns(any())).thenReturn(ApiResult.success(CtaPatternResponse(body)))

        dataSource.getBusRouteWaypoints("22") shouldBe
            listOf(LatLng(1.0, 1.0), LatLng(2.0, 2.0), LatLng(3.0, 3.0))
    }
}
