package com.taitsmith.busboy.ui

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.di.SettingsRepository
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.ByIdViewModel.BusState
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment: Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
    OnMapsSdkInitializedCallback {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val args: MapsFragmentArgs by navArgs()
    private val byIdViewModel: ByIdViewModel by activityViewModels()
    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    private lateinit var polylineCoords: List<LatLng>
    private lateinit var locationChoice: LatLng
    private lateinit var googleMap: GoogleMap
    private var busMarker: Marker? = null
    //show the "no bus data" notice at most once per map view, so a persistently-failing 60s poll
    //doesn't spam a snackbar every minute.
    private var busErrorShown = false

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap

        googleMap.clear()

        /* we'll show the map fragment in three cases:
            - a bus route with the location of the bus
            - walking directions from user's location to a bus stop
            - allowing a user to pick a location to display nearby stops
            we'll take polyline coordinate from the viewmodels in the first two cases,
            otherwise we'll focus on downtown oakland and let users move the map to pick a spot
         */
        polylineCoords = when (args.polylineType) {
            "directions" -> nearbyViewModel.directionPolylineCoords.value!!
            "route" -> byIdViewModel.busRouteWaypoints.value!!
            "choice" -> mutableListOf(defaultMapCenter())
            else -> mutableListOf()
        }

        if (polylineCoords.size == 1) {
            setupForLocationChoice()
        } else {
            setupForRouteDisplay()
        }
    }

    //the "choose on map" flow needs a starting camera position; center on the selected agency's
    //downtown (Chicago Loop for CTA, downtown Oakland for AC Transit) so the user isn't panning
    //across the country to find their city.
    private fun defaultMapCenter(): LatLng =
        if (settingsRepository.selectedAgencyState.value == Agency.CTA) LatLng(41.8786, -87.6251)
        else LatLng(37.811, -122.268)

    //if we're letting user pick a location we want it empty
    private fun setupForLocationChoice() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylineCoords[0], 15F))
        locationChoice = polylineCoords[0]

        googleMap.addMarker(
            MarkerOptions()
                .position(polylineCoords[0])
                .draggable(true)
                .title(getString(R.string.map_you_are_here))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        )

        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMarkerDragListener(this)

        view?.rootView?.let {
            Snackbar.make(it, R.string.snackbar_map_long_press_drag, Snackbar.LENGTH_LONG)
                .show()
        }

    }

    //if we're displaying a route
    private fun setupForRouteDisplay() {
        val directionRoute = googleMap.addPolyline(PolylineOptions())
        directionRoute.points = polylineCoords
        directionRoute.color = Color.RED
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylineCoords[0], 15F))

        //start location
        googleMap.addMarker(
            MarkerOptions()
                .position(polylineCoords[0])
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        //end location
        googleMap.addMarker(
            MarkerOptions()
                .position(polylineCoords[polylineCoords.size - 1])
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                byIdViewModel.bus.collect {
                    when (it) {
                        is BusState.Error -> if (!busErrorShown) {
                            busErrorShown = true
                            view?.let { v ->
                                Snackbar.make(v, R.string.snackbar_null_bus_coords, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        is BusState.Initial -> {
                            val latLng = LatLng(it.bus.latitude!!, it.bus.longitude!!)
                            val options = MarkerOptions()
                                .position(latLng)
                                .title("the bus")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            busMarker = googleMap.addMarker(options)
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
                        }
                        is BusState.Updated -> updateBusMarker(it.bus)
                        is BusState.Detail -> {}
                        BusState.Loading -> {}
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context?.let { MapsInitializer.initialize(it, Renderer.LATEST, this) }
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    private fun updateBusMarker(bus: Bus) {
        val latLng = LatLng(bus.latitude!!, bus.longitude!!)
        busMarker!!.position = latLng
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        //googleMap is only assigned inside the async OnMapReadyCallback; if the fragment is destroyed
        //before the map finishes loading, the lateinit was never set- guard before touching it.
        if (::googleMap.isInitialized) {
            googleMap.clear()
            googleMap.setOnMarkerDragListener(null)
            googleMap.setOnMarkerClickListener(null)
        }
    }

    override fun onMarkerDrag(p0: Marker) {
        //do nothing
    }

    override fun onMarkerDragEnd(p0: Marker) {
        locationChoice = p0.position
        val loc = Location(null)
        loc.latitude = p0.position.latitude
        loc.longitude = p0.position.longitude
        nearbyViewModel.setLocation(loc)
        view?.let {
            Snackbar.make(it, R.string.snackbar_map_click_to_select, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    override fun onMarkerDragStart(p0: Marker) {
        view?.let {
            Snackbar.make(it, R.string.snackbar_map_drop, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        //tapping the marker commits its current position (the snackbar says "click to select").
        //without this, a user who taps the default marker instead of dragging returns with
        //currentLocation still at 0.0 and the search button never enabled. onMarkerClick only
        //fires in the "choice" flow (only setupForLocationChoice registers this listener).
        val loc = Location(null)
        loc.latitude = p0.position.latitude
        loc.longitude = p0.position.longitude
        nearbyViewModel.setLocation(loc)
        findNavController().navigate(R.id.nearbyFragment)
        return false
    }

    override fun onMapsSdkInitialized(p0: Renderer) {
        //required by OnMapsSdkInitializedCallback; nothing to do once the SDK reports its renderer.
    }
}