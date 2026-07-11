package com.taitsmith.busboy.di

import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.data.Agency
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private class FakeSettingsRepository(initial: Agency) : SettingsRepository {
        private val state = MutableStateFlow(initial)
        override val selectedAgency: StateFlow<Agency> = state
        override val selectedAgencyState: StateFlow<Agency> = state
        override suspend fun setAgency(agency: Agency) { state.value = agency }
    }

    private val acResponse = BustimeResponse()
    private val ctaResponse = BustimeResponse()

    private val acSource: RemoteDataSource = mock {
        whenever(it.predictions(anyOrNull(), anyOrNull())).thenReturn(flowOf(acResponse))
    }
    private val ctaSource: RemoteDataSource = mock {
        whenever(it.predictions(anyOrNull(), anyOrNull())).thenReturn(flowOf(ctaResponse))
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
}
