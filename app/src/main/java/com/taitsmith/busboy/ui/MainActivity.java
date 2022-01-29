package com.taitsmith.busboy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.snackbar.Snackbar;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.ActivityMainBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import im.delight.android.location.SimpleLocation;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.OnListItemSelectedListener, MainActivityFragment.OnListItemLongListener{

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6;
    private ActivityMainBinding binding;

    static MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        viewModel.mutableStatusMessage.observe(this, this::getStatus);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.fragment.getId(), new MainActivityFragment())
                    .commitNow();
        }
    }

    //let people know what's happening
    private void getStatus(String s) {
        switch (s) {
            case "NO_PERMISSION" : //we don't have permission to access location
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
                break;
            case "NEARBY_404" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_nearby_404,
                        Snackbar.LENGTH_LONG).show();
                break;
            case "NO_LOC_ENABLED" : //we have permission but location setting isn't on
                askToEnableLoc();
                break;
            case "BAD_INPUT" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_bad_input,
                        Snackbar.LENGTH_LONG).show();
                break;
            case "NULL_PRED_RESPONSE" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_no_predictions,
                        Snackbar.LENGTH_LONG).show();
                break;
            case "HELP_REQUESTED" :
                showHelp();
                break;
            case "FAVORITES" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_favorites_in_progress,
                        Snackbar.LENGTH_LONG).show();
                break;
            case "POLYLINE_READY" :
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
        }
    }

    private void askToEnableLoc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(R.string.dialog_no_location)
                .setPositiveButton(R.string.dialog_no_loc_positive, (dialogInterface, i) ->
                        SimpleLocation.openSettings(this))
                .setNegativeButton(R.string.dialog_no_loc_negative, null)
                .create()
                .show();
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_help)
                    .setPositiveButton(R.string.dialog_got_it, null)
                    .create()
                    .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPredictionSelected(int position) {
    }

    @Override
    public void onNearbyStopSelected(int position) {
        viewModel.getStopPredictions(viewModel.stopList.get(position).getStopId());
    }

    @SuppressLint("MissingPermission")
    public static void getLocation(FusedLocationProviderClient client) {
        client.getCurrentLocation(LocationRequest.QUALITY_LOW_POWER, null)
                .addOnSuccessListener(location -> {
                    viewModel.loc = location;
                    viewModel.getNearbyStops();
                });
    }

    @Override
    public void onNearbyLongSelected(int position) {
        Stop stop = viewModel.stopList.get(position);
        String startLoc = Double.toString(viewModel.loc.getLatitude()).concat(",")
                .concat(Double.toString(viewModel.loc.getLongitude()));
        String endLoc = Double.toString(stop.getLatitude()).concat(",")
                .concat(Double.toString(stop.getLongitude()));

        viewModel.getDirectionsToStop(startLoc, endLoc);
    }

    @Override
    public void onPredictionLongSelected(int position) {
    }
}
