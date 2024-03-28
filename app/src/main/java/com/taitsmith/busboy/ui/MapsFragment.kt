package com.taitsmith.busboy.ui

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel

class MapsFragment: Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
    OnMapsSdkInitializedCallback {

    private val args: MapsFragmentArgs by navArgs()
    private val byIdViewModel: ByIdViewModel by activityViewModels()
    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    private lateinit var polylineCoords: List<LatLng>
    private lateinit var locationChoice: LatLng
    private lateinit var bus: Bus
    private lateinit var googleMap: GoogleMap

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
            "choice" -> mutableListOf(LatLng(37.811, -122.268))
            else -> mutableListOf()
        }

        if (polylineCoords.size == 1) {
            setupForLocationChoice()
        } else {
            setupForRouteDisplay()
        }
    }

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

        //only want to do this if we're showing a bus route
        if (args.polylineType == "route") {
            bus = byIdViewModel.bus.value!!
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(bus.latitude!!, bus.longitude!!))
                    .title("THE BUS")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(bus.latitude!!, bus.longitude!!), 15F))
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        googleMap.clear()
        googleMap.setOnMarkerDragListener(null)
        googleMap.setOnMarkerClickListener(null)
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
        findNavController().navigate(R.id.nearbyFragment)
        return false
    }

    override fun onMapsSdkInitialized(p0: Renderer) {
        when (p0) {
            //we should always get the latest but sometimes there's a weird
            //issue with legacy renderer being loaded which causes issues.
            Renderer.LATEST -> Log.d("MAPS", "latest renderer")
            Renderer.LEGACY -> Log.d("MAPS", "legacy renderer")
        }
    }
}