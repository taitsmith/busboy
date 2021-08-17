package com.taitsmith.busboy.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;


import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.obj.StopPredictionResponse;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import java.util.List;

public class MainActivityFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    //TODO rename these
    private MainActivityViewModel mainActivityViewModel;
    private FragmentMainActivityBinding binding;
    Button searchByIdButton, searchNearbyButton;
    EditText stopIdEditText;
    ListView predictionListView;
    PredictionAdapter adapter;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 0;

    public static MainActivityFragment newInstance() {
        return new MainActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainActivityBinding.inflate(inflater, container, false);
        searchByIdButton = binding.searchIdButton;
        searchNearbyButton = binding.searchNearbyButton;
        stopIdEditText = binding.stopEntryEditText;
        predictionListView = binding.busListView;

        searchByIdButton.setOnClickListener(view -> {
            mainActivityViewModel.stopId = stopIdEditText.getText().toString();
            mainActivityViewModel.getMutableLivePrediction();
        });

        searchNearbyButton.setOnClickListener(view -> mainActivityViewModel.getLocation());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivityViewModel = new MainActivityViewModel(requireActivity().getApplication());

        mainActivityViewModel.mutableLivePrediction.observe(getViewLifecycleOwner(), predictions -> {
            adapter = new PredictionAdapter(predictions);
            predictionListView.setAdapter(adapter);
        });
        locationPermission();
    }

    private void hideUI(boolean isHidden) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "ok", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void locationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_FINE_LOCATION);
        }
    }
}
