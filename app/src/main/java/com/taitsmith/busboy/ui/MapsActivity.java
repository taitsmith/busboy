package com.taitsmith.busboy.ui;

import androidx.fragment.app.FragmentActivity;


import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.ActivityMapsBinding;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.polylineCoords;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    Polyline directionRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (polylineCoords.size() == 0) {
            Snackbar.make(binding.getRoot(), R.string.snackbar_no_polyline,
                    Snackbar.LENGTH_LONG).show();
        } else {
            directionRoute = googleMap.addPolyline(new PolylineOptions());
            directionRoute.setPoints(polylineCoords);
            directionRoute.setColor(Color.RED);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylineCoords.get(0), 15));
        }

        googleMap.addMarker(new MarkerOptions()
        .position(polylineCoords.get(0))
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.addMarker(new MarkerOptions()
        .position(polylineCoords.get(polylineCoords.size() - 1))
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
}