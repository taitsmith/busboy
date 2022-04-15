package com.taitsmith.busboy.ui;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.data.Bus;
import com.taitsmith.busboy.databinding.ActivityMapsBinding;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.polylineCoords;
import static com.taitsmith.busboy.ui.MainActivity.mutableBus;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap googleMap;
    ActivityMapsBinding binding;
    Polyline directionRoute;
    LatLng cameraFocus; //these depends on whether we're
    float zoom;         //displaying a route or direction map
    Bus bus;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().getStringExtra("POLYLINE_TYPE").equals("ROUTE")) {
            bus = mutableBus.getValue();
            try {
                zoom = 15;
                cameraFocus = new LatLng(bus.latitude, bus.longitude);
            } catch (NullPointerException e) {
                mutableErrorMessage.setValue("NULL_BUS_COORDS");
            }
        } else {
            cameraFocus = polylineCoords.get(0);
            zoom = 15;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.clear();

        if (polylineCoords.size() == 0) {
            Snackbar.make(binding.getRoot(), R.string.snackbar_no_polyline,
                    Snackbar.LENGTH_LONG).show();
        } else {
            directionRoute = googleMap.addPolyline(new PolylineOptions());
            directionRoute.setPoints(polylineCoords);
            directionRoute.setColor(Color.RED);
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraFocus, zoom));
            } catch (NullPointerException e) {
                e.printStackTrace();
                mutableErrorMessage.setValue("NULL_BUS_COORDS");
            }
        }

        googleMap.addMarker(new MarkerOptions()
            .position(polylineCoords.get(0))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.addMarker(new MarkerOptions()
            .position(polylineCoords.get(polylineCoords.size() - 1))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        try { //we should really check to make sure the bus coords aren't null before this
            googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(bus.getLatitude(), bus.getLongitude()))
                    .title("THE BUS")
                     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } catch (NullPointerException e) {
            mutableErrorMessage.setValue("NULL_BUS_COORDS");
            onBackPressed();
        }
    }
}