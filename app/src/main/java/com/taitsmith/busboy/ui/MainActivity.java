package com.taitsmith.busboy.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.material.tabs.TabLayout;
import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.ActivityMainBinding;
import com.taitsmith.busboy.obj.Bus;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.viewmodels.ByIdViewModel;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;
import com.taitsmith.busboy.viewmodels.NearbyViewModel;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;


import dagger.hilt.android.AndroidEntryPoint;
import im.delight.android.location.SimpleLocation;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity
        implements OnItemClickListener, OnItemLongClickListener {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 6;

    private ActivityMainBinding binding;

    static MainActivityViewModel mainActivityViewModel;

    public static String acTransitApiKey;
    public static MutableLiveData<Bus> mutableBus;
    public static MutableLiveData<String> mutableNearbyStatusUpdater;

    TabLayout mainTabLayout;
    NearbyFragment nearbyFragment;
    ByIdFragment byIdFragment;
    FavoritesFragment favoritesFragment;
    FragmentManager fragmentManager;
    Prediction prediction;
    TextView nearbyStatusUpdateTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        acTransitApiKey = getString(R.string.ac_transit_key);
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mainTabLayout = binding.mainTabLayout;
        nearbyStatusUpdateTv = binding.nearbyStatusUpdater;

        mutableBus = new MutableLiveData<>();
        mutableNearbyStatusUpdater = new MutableLiveData<>();

        fragmentManager = getSupportFragmentManager();
        nearbyFragment = new NearbyFragment();
        byIdFragment = new ByIdFragment();
        favoritesFragment = new FavoritesFragment();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(binding.mainFragmentContainer.getId(), byIdFragment)
                    .commit();
        }

        setObservers();
        setTabListeners();
    }

    private void setObservers() {
        mutableStatusMessage.observe(this, this::getStatusMessage);
        mutableErrorMessage.observe(this, this::getErrorMessage);
        mutableBus.observe(this, bus ->
                mainActivityViewModel.getWaypoints(prediction.getRt()));
        mutableNearbyStatusUpdater.observe(this, this::updateNearbyStatusText);
    }

    private void setTabListeners() {
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setFragments(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setFragments(tab.getText().toString());
            }
        });
    }
    private void setFragments(String id) {
        switch (id) {
            case "By ID":
                fragmentManager.beginTransaction()
                        .replace(binding.mainFragmentContainer.getId(), byIdFragment)
                        .commit();
                break;
            case "Nearby":
                fragmentManager.beginTransaction()
                        .replace(binding.mainFragmentContainer.getId(), nearbyFragment)
                        .commit();
                break;
            case "Favorites":
                Snackbar.make(binding.getRoot(), R.string.snackbar_favorites_in_progress,
                        BaseTransientBottomBar.LENGTH_LONG).show();
                break;
            case "Help":
                MainActivityViewModel.mutableStatusMessage.setValue("HELP_REQUESTED");
                break;
        }
    }
    private void getErrorMessage(String s) {
        hideUi(false);
        switch (s) {
            case "NO_PERMISSION" : //we don't have permission to access location
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
                break;
            case "404" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_404,
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
            case "NULL_BUS_COORDS" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_null_bus_coords,
                        BaseTransientBottomBar.LENGTH_LONG).show();
                break;
            case "CALL_FAILURE" :
                Snackbar.make(binding.getRoot(), R.string.snackbar_network_error,
                        Snackbar.LENGTH_LONG).show();
                break;
            case "BAD_DISTANCE" :
                Snackbar.make(binding.getRoot(),R.string.snackbar_bad_distance,
                        BaseTransientBottomBar.LENGTH_LONG).show();
                break;
        }
    }

    private void getStatusMessage(String s) {
        Intent intent = new Intent(this, MapsActivity.class);
        switch (s) {
            case "HELP_REQUESTED" :
                showHelp();
                break;
            case "DIRECTION_POLYLINE_READY" :
                intent.putExtra("POLYLINE_TYPE", "DIRECTION");
                startActivity(intent);
                break;
            case "ROUTE_POLYLINE_READY" :
                mutableStatusMessage.setValue("LOADED");
                intent.putExtra("POLYLINE_TYPE", "ROUTE");
                startActivity(intent);
                break;
            case "LOADING":
                hideUi(true);
                break;
            case "LOADED":
                hideUi(false);
                break;
        }
    }

    private void hideUi(boolean shouldHide) {
        if (shouldHide) {
            binding.mainFragmentContainer.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.mainFragmentContainer.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
            nearbyStatusUpdateTv.setVisibility(View.INVISIBLE);
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
                .setNegativeButton(R.string.dialog_no_loc_negative, ((dialogInterface, i) -> {
                    Snackbar.make(binding.getRoot(), R.string.snackbar_location_disabled,
                            Snackbar.LENGTH_LONG).show();
                }))
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

    private void updateNearbyStatusText(String s) {
        nearbyStatusUpdateTv.setVisibility(View.VISIBLE);
        nearbyStatusUpdateTv.setText(getString(R.string.nearby_status_update, s));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NearbyViewModel.mutableSimpleLocation.setValue(NearbyViewModel.loc);
                NearbyViewModel.loc.beginUpdates();
                NearbyViewModel.loc.setListener(() ->
                        NearbyViewModel.mutableSimpleLocation.setValue(NearbyViewModel.loc));
            }
        }
    }

    @Override
    public void onNearbyItemSelected(int position) {
        mutableStatusMessage.setValue("LOADING");
        Bundle bundle = new Bundle();
        bundle.putString("BY_ID", NearbyViewModel.stopList.get(position).getStopId());
        byIdFragment.setArguments(bundle);
        ByIdViewModel.predictionList.clear();
        fragmentManager.beginTransaction()
                .replace(binding.mainFragmentContainer.getId(), byIdFragment)
                .addToBackStack(null)
                .commit();
        mainTabLayout.setScrollPosition(0, 0, true);
    }

    @Override
    public void onIdItemSelected(int position) {
        prediction = byIdFragment.predictionList.get(position);
        mutableStatusMessage.setValue("LOADING");
        mainActivityViewModel.getBusLocation(byIdFragment.predictionList.get(position).getVid());
    }

    @Override
    public void onNearbyLongClick(int position) {
        mutableStatusMessage.setValue("LOADING");
        Stop stop = NearbyViewModel.stopList.get(position);
        String start = Double.toString(NearbyViewModel.loc.getLatitude()).concat(",")
                .concat(Double.toString(NearbyViewModel.loc.getLongitude()));
        String end = Double.toString(stop.getLatitude()).concat(",")
                .concat(Double.toString(stop.getLongitude()));
        mainActivityViewModel.getDirectionsToStop(start, end);
    }

    @Override
    public void onIdLongClick(int position) {
    }
}