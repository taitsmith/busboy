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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.utils.NearbyAdapter;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import java.util.List;

public class MainActivityFragment extends Fragment{

    //TODO rename these
    private MainActivityViewModel mainActivityViewModel;
    FragmentMainActivityBinding binding;
    Button searchByIdButton, searchNearbyButton;
    EditText stopIdEditText;
    ListView listView; //doubles
    PredictionAdapter predictionAdapter;
    NearbyAdapter nearbyAdapter;

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
        listView = binding.predOrNearbyListView;

        searchByIdButton.setOnClickListener(view -> {
            String s = stopIdEditText.getText().toString();
            if (s.length() != 5) mainActivityViewModel.errorMessage.setValue("BAD_INPUT");
            else {
                showLoading(true);
                mainActivityViewModel.stopId = stopIdEditText.getText().toString();
                mainActivityViewModel.getMutableLivePrediction();
            }
        });

        searchNearbyButton.setOnClickListener(view -> {
            showLoading(true);
            mainActivityViewModel.checkLocationPerm();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        setObservers();
    }

    private void setObservers() {
        mainActivityViewModel.mutableLivePrediction.observe(getViewLifecycleOwner(), predictions -> {
            predictionAdapter = new PredictionAdapter(predictions);
            listView.setAdapter(predictionAdapter);
            showLoading(false);
        });

        mainActivityViewModel.mutableLiveStop.observe(getViewLifecycleOwner(), stopList -> {
            nearbyAdapter = new NearbyAdapter(stopList);
            listView.setAdapter(nearbyAdapter);
            showLoading(false);
        });
    }

    //let the user know some stuff if happening in the background
    private void showLoading(boolean isHidden) {
        binding.loadingBar.setVisibility(isHidden ? View.VISIBLE : View.INVISIBLE);
    }
}
