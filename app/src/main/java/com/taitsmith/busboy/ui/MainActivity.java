package com.taitsmith.busboy.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.material.tabs.TabLayout;
import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.ActivityMainBinding;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

import im.delight.android.location.SimpleLocation;

public class MainActivity extends AppCompatActivity
        implements OnItemClickListener, OnItemLongClickListener {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6;

    private ActivityMainBinding binding;

    static MainActivityViewModel mainActivityViewModel;
    public static String acTransitApiKey;

    TabLayout mainTabLayout;
    NearbyFragment nearbyFragment;
    ByIdFragment byIdFragment;
    FavoritesFragment favoritesFragment;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        acTransitApiKey = getString(R.string.api_token);
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);


        mainTabLayout = binding.mainTabLayout;

        fragmentManager = getSupportFragmentManager();
        nearbyFragment = new NearbyFragment();
        byIdFragment = new ByIdFragment();
        favoritesFragment = new FavoritesFragment();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(binding.mainFragmentContainer.getId(), byIdFragment)
                    .commit();
        }
        mutableStatusMessage.observe(this, this::getStatusMessage);
        mutableErrorMessage.observe(this, this::getErrorMessage);
        setTabListeners();
    }

    private void setTabListeners() {
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getText().toString()) {
                    case "By ID":
                        fragmentManager.beginTransaction()
                                .replace(binding.mainFragmentContainer.getId(), byIdFragment)
                                .addToBackStack(null)
                                .commit();
                        break;
                    case "Nearby":

                        fragmentManager.beginTransaction()
                                .replace(binding.mainFragmentContainer.getId(), nearbyFragment)
                                .addToBackStack(null)
                                .commit();
                        break;
                    case "Favorites":
                        fragmentManager.beginTransaction()
                                .replace(binding.mainFragmentContainer.getId(),favoritesFragment)
                                .commit();
                        break;
                    case "Help":
                        MainActivityViewModel.mutableStatusMessage.setValue("HELP_REQUESTED");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getErrorMessage(String s) {
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
        }
    }

    private void getStatusMessage(String s) {
        switch (s) {
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
                break;
            case "LOADING":
                binding.mainFragmentContainer.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
                break;
            case "LOADED":
                binding.mainFragmentContainer.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         mutableStatusMessage.removeObservers(this);
         binding = null;
         mainActivityViewModel = null;
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
                getLocation(new FusedLocationProviderClient(this));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    public static void getLocation(FusedLocationProviderClient client) {
        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    mainActivityViewModel.loc = location;
//                    mainActivityViewModel.getNearbyStops();
                });
    }

    @Override
    public void onNearbyItemSelected(int position) {

    }

    @Override
    public void onIdItemSelected(int position) {
        Toast.makeText(this, "BY ID", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNearbyLongClick(int position) {

    }

    @Override
    public void onIdLongClick(int position) {
        Toast.makeText(this, "BY ID LONGBOI", Toast.LENGTH_SHORT).show();
    }
}
