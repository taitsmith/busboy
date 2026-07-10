package com.taitsmith.busboy.api

import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.di.ApiRepositoryImpl
import com.taitsmith.busboy.di.RemoteDataSource
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Focused tests for the prediction-text transform in [ApiRepositoryImpl.stopPredictions]:
 * "1"/"Due" -> "Arriving", everything else -> "in X minutes", plus the "No service scheduled"
 * and generic error branches. Uses a mocked [RemoteDataSource] so the raw prdctdn/dyn/error
 * values are fully controllable (the shared FakeRemoteDataSource hard-codes prdctdn = "10").
 */
@RunWith(JUnit4::class)
class ApiRepositoryTransformTest {

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var apiRepository: ApiRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mock()
        apiRepository = ApiRepositoryImpl(remoteDataSource)
    }

    private fun stubPredictions(vararg predictions: Prediction) {
        val btr = BustimeResponse().apply { prd = predictions.toList() }
        whenever(remoteDataSource.predictions(any(), anyOrNull())).thenReturn(flowOf(btr))
    }

    @Test
    fun `prdctdn of Due becomes Arriving`() = runTest {
        stubPredictions(Prediction(stpnm = "Broadway & 25th St", prdctdn = "Due", dyn = 0))

        apiRepository.stopPredictions("1", null).first()[0].prdctdn shouldBe "Arriving"
    }

    @Test
    fun `prdctdn of 1 becomes Arriving`() = runTest {
        stubPredictions(Prediction(stpnm = "Broadway & 25th St", prdctdn = "1", dyn = 0))

        apiRepository.stopPredictions("1", null).first()[0].prdctdn shouldBe "Arriving"
    }

    @Test
    fun `prdctdn of DUE (CTA uppercase) becomes Arriving`() = runTest {
        stubPredictions(Prediction(stpnm = "Clark & Addison", prdctdn = "DUE", dyn = 0))

        apiRepository.stopPredictions("1", null).first()[0].prdctdn shouldBe "Arriving"
    }

    @Test
    fun `numeric prdctdn becomes in X minutes`() = runTest {
        stubPredictions(Prediction(stpnm = "Broadway & 25th St", prdctdn = "10", dyn = 0))

        apiRepository.stopPredictions("1", null).first()[0].prdctdn shouldBe "in 10 minutes"
    }

    @Test
    fun `each prediction in the list is transformed independently`() = runTest {
        stubPredictions(
            Prediction(stpnm = "s", prdctdn = "Due", dyn = 0),
            Prediction(stpnm = "s", prdctdn = "7", dyn = 0)
        )

        val result = apiRepository.stopPredictions("1", null).first()

        result[0].prdctdn shouldBe "Arriving"
        result[1].prdctdn shouldBe "in 7 minutes"
    }

    @Test
    fun `non-stopping buses (non-zero dyn) are filtered out`() = runTest {
        stubPredictions(
            Prediction(stpnm = "stopping", prdctdn = "5", dyn = 0),
            Prediction(stpnm = "not stopping", prdctdn = "5", dyn = 1)
        )

        val result = apiRepository.stopPredictions("1", null).first()

        result.size shouldBe 1
        result[0].stpnm shouldBe "stopping"
    }

    @Test
    fun `predictions with null dyn are treated as stopping and kept`() = runTest {
        stubPredictions(Prediction(stpnm = "unknown dyn", prdctdn = "5", dyn = null))

        apiRepository.stopPredictions("1", null).first().size shouldBe 1
    }

    @Test
    fun `error of No service scheduled throws NO_SERVICE_SCHEDULED`() = runTest {
        val error = BustimeResponse.BusError().apply { msg = "No service scheduled" }
        val btr = BustimeResponse().apply { this.error = listOf(error) }
        whenever(remoteDataSource.predictions(any(), anyOrNull())).thenReturn(flowOf(btr))

        val msg = try {
            apiRepository.stopPredictions("1", null).first()
            null
        } catch (e: Exception) {
            e.message
        }
        msg shouldBe "NO_SERVICE_SCHEDULED"
    }

    @Test
    fun `any other error message throws UNKNOWN`() = runTest {
        val error = BustimeResponse.BusError().apply { msg = "Transaction limit exceeded" }
        val btr = BustimeResponse().apply { this.error = listOf(error) }
        whenever(remoteDataSource.predictions(any(), anyOrNull())).thenReturn(flowOf(btr))

        val msg = try {
            apiRepository.stopPredictions("1", null).first()
            null
        } catch (e: Exception) {
            e.message
        }
        msg shouldBe "UNKNOWN"
    }
}
