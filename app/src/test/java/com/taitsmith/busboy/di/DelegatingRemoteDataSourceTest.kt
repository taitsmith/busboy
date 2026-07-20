package com.taitsmith.busboy.di

import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.data.Bus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.inject.Provider

/**
 * Verifies the provider-routing contract: [DelegatingRemoteDataSource] forwards to the concrete
 * RemoteDataSource for the currently selected [Agency], and re-routes when that selection flips
 * mid-session (agency read at collection time, not construction).
 */
class DelegatingRemoteDataSourceTest {

    private val acResponse = BustimeResponse()
    private val ctaResponse = BustimeResponse()
    private val acAlerts = ServiceAlertResponse(BustimeResponse())
    private val ctaAlerts = ServiceAlertResponse(BustimeResponse())
    private val acBus = Bus().apply { vehicleId = 1 }
    private val ctaBus = Bus().apply { vehicleId = 2 }

    private val acSource: RemoteDataSource = mock {
        whenever(it.predictions(anyOrNull(), anyOrNull())).thenReturn(flowOf(acResponse))
        whenever(it.serviceAlerts(anyOrNull())).thenReturn(flowOf(acAlerts))
    }
    private val ctaSource: RemoteDataSource = mock {
        whenever(it.predictions(anyOrNull(), anyOrNull())).thenReturn(flowOf(ctaResponse))
        whenever(it.serviceAlerts(anyOrNull())).thenReturn(flowOf(ctaAlerts))
    }

    private val sources = mapOf(
        Agency.AC_TRANSIT to Provider { acSource },
        Agency.CTA to Provider { ctaSource }
    )

    @Test
    fun `routes to the selected agency`() = runTest {
        val settings = FakeSettingsRepository(Agency.AC_TRANSIT)
        val delegate = DelegatingRemoteDataSource(sources, settings)

        delegate.predictions("1", null).first() shouldBe acResponse
    }

    @Test
    fun `re-routes when the selected agency changes mid-session`() = runTest {
        val settings = FakeSettingsRepository(Agency.AC_TRANSIT)
        val delegate = DelegatingRemoteDataSource(sources, settings)

        delegate.predictions("1", null).first() shouldBe acResponse

        settings.setAgency(Agency.CTA)
        delegate.predictions("1", null).first() shouldBe ctaResponse
    }

    @Test
    fun `routes serviceAlerts to the selected agency`() = runTest {
        val delegate = DelegatingRemoteDataSource(sources, FakeSettingsRepository(Agency.CTA))

        delegate.serviceAlerts("1").first() shouldBe ctaAlerts
    }

    @Test
    fun `routes and re-routes a suspend call (getDetailedBusInfo)`() = runTest {
        whenever(acSource.getDetailedBusInfo(anyOrNull())).thenReturn(acBus)
        whenever(ctaSource.getDetailedBusInfo(anyOrNull())).thenReturn(ctaBus)

        val settings = FakeSettingsRepository(Agency.AC_TRANSIT)
        val delegate = DelegatingRemoteDataSource(sources, settings)

        delegate.getDetailedBusInfo("1") shouldBe acBus
        settings.setAgency(Agency.CTA)
        delegate.getDetailedBusInfo("1") shouldBe ctaBus
    }
}
