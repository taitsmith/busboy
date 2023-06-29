package com.taitsmith.busboy.viewmodels

import android.app.Application
import android.location.Location
import android.location.LocationProvider
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.BuildConfig
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.getOrAwaitValue
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class NearbyViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    @Mock
    private lateinit var apiRepository: ApiRepository
    @Mock
    private lateinit var application: Application
    @Mock
    private lateinit var location: Location

    private lateinit var nearbyViewModel: NearbyViewModel
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private var mockedWaypoints = mutableListOf<LatLng>()
    private var mockedNearbyStops = mutableListOf<Stop>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        createMockedStops()
        createMockedWaypoints()

        mainActivityViewModel = MainActivityViewModel(application)
        nearbyViewModel = NearbyViewModel(application, apiRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        testDispatcher.cleanupTestCoroutines()
    }

    private fun createMockedStops() {
        val stop1 = Stop(id = 1, stopId = "55555")
        val stop2 = Stop(id = 2, stopId = "58829")
        val stop3 = Stop(id = 3, stopId = "56669")

        mockedNearbyStops.add(0, stop1)
        mockedNearbyStops.add(1, stop2)
        mockedNearbyStops.add(2, stop3)
    }

    private fun createMockedWaypoints() {
        val latlng1 = LatLng(1.1,1.1)
        val latlng2 = LatLng(2.2, 2.2)
        mockedWaypoints.add(0, latlng1)
        mockedWaypoints.add(1, latlng2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test nearby stops added to livedata`() = testDispatcher.runBlockingTest {
        `when`(apiRepository.getNearbyStops(location.latitude, location.longitude, 1000, true, null))
            .thenReturn(mockedNearbyStops)
        `when`(apiRepository.getLinesServedByStop(mockedNearbyStops)).thenReturn(mockedNearbyStops)

        `when`(location.longitude).thenReturn(1.1)
        `when`(location.latitude).thenReturn(1.1)

        nearbyViewModel.setLocation(location)

        nearbyViewModel.getNearbyStops()
        val returnedStops = apiRepository.getNearbyStops(1.1, 1.1, 1000, true, null)
        assertEquals(returnedStops, nearbyViewModel.nearbyStops.getOrAwaitValue())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test get directions added to live data`() = testDispatcher.runBlockingTest {
        `when`(apiRepository.getDirectionsToStop("1", "1"))
            .thenReturn(mockedWaypoints)

        nearbyViewModel.getDirectionsToStop("1", "1")

        val returnedWaypoints = apiRepository.getDirectionsToStop("1", "1")

        assertEquals(mockedWaypoints, returnedWaypoints)
        assertEquals(false, nearbyViewModel.isUpdated.getOrAwaitValue())
        assertEquals(returnedWaypoints, nearbyViewModel.directionPolylineCoords.getOrAwaitValue())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test isUpdated is updated`() = testDispatcher.runBlockingTest {
        nearbyViewModel.setIsUpdated(true)
        assertEquals(true, nearbyViewModel.isUpdated.getOrAwaitValue())
    }

}