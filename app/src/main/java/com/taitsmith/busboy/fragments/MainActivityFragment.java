package com.taitsmith.busboy.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

public class MainActivityFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    //TODO rename these
    private MainActivityViewModel mainActivityViewModel;
    private FragmentMainActivityBinding binding;
    Button searchByIdButton, searchNearbyButton;
    EditText stopIdEditText;
    ListView predictionListView;
    PredictionAdapter adapter;

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
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        mainActivityViewModel.mutableLivePrediction.observe(getViewLifecycleOwner(), predictions -> {
            adapter = new PredictionAdapter(predictions);
            predictionListView.setAdapter(adapter);
        });
    }

    private void hideUI(boolean isHidden) {

    }
}
