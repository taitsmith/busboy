package com.taitsmith.busboy.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.taitsmith.busboy.R
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel

class MapsFragment : Fragment() {
    private val args: MapsFragmentArgs by navArgs()
    private val byIdViewModel: ByIdViewModel by activityViewModels()
    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    private lateinit var polylineCoords: List<LatLng>
    private lateinit var cameraFocus: LatLng

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.clear()
        val bus = byIdViewModel.bus.value

        when (args.polylineType) {
            "directions" -> polylineCoords = nearbyViewModel.directionPolylineCoords.value!!
            "route" -> polylineCoords = byIdViewModel.busRouteWaypoints.value!!
        }

        cameraFocus = if (args.polylineType == "route") LatLng(bus?.latitude!!, bus.longitude!!)
        else polylineCoords[0]

        MainActivityViewModel.mutableStatusMessage.value = "LOADED"

        if (polylineCoords.isEmpty()) {
            MainActivityViewModel.mutableErrorMessage.value = "404"
        } else {
            val directionRoute = googleMap.addPolyline(PolylineOptions())
            directionRoute.points = polylineCoords
            directionRoute.color = Color.RED
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraFocus, 15F))
            } catch (e: NullPointerException) {
                e.printStackTrace()
                MainActivityViewModel.mutableErrorMessage.setValue("NULL_BUS_COORDS")
            }
        }

        googleMap.addMarker(
            MarkerOptions()
                .position(polylineCoords[0])
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        googleMap.addMarker(
            MarkerOptions()
                .position(polylineCoords[polylineCoords.size - 1])
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        if (bus != null) { //only want to do this if we're showing a bus route
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(bus.latitude!!, bus.longitude!!))
                    .title("THE BUS")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}