package com.taitsmith.busboy.ui;

import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;

import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.utils.NearbyAdapter;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

public class MainActivityFragment extends Fragment{

    MainActivityViewModel mainActivityViewModel;
    FragmentMainActivityBinding binding;
    Button searchByIdButton, searchNearbyButton;
    EditText stopIdEditText;
    ListView listView; //doubles
    PredictionAdapter predictionAdapter;
    NearbyAdapter nearbyAdapter;
    OnListItemSelectedListener listener;

    public MainActivityFragment newInstance() {
        return new MainActivityFragment();
    }

    public interface OnListItemSelectedListener {
        void onPredictionSelected(int position);
        void onNearbyStopSelected(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnListItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "Must Implement OnListItemSelectedListener");
        }
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
                    mainActivityViewModel.getStopPredictions(s);
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
        mainActivityViewModel.mutableStopPredictions.observe(getViewLifecycleOwner(), predictions -> {
            predictionAdapter = new PredictionAdapter(predictions);
            listView.setAdapter(predictionAdapter);
            listView.setOnItemClickListener((adapterView, view, i, l) ->
                    listener.onPredictionSelected(i));
            showLoading(false);
        });

        mainActivityViewModel.mutableNearbyStops.observe(getViewLifecycleOwner(), stopList -> {
            nearbyAdapter = new NearbyAdapter(stopList);
            listView.setAdapter(nearbyAdapter);
            listView.setOnItemClickListener((adapterView, view, i, l) ->
                    listener.onNearbyStopSelected(i));
            showLoading(false);
        });
    }

    //let the user know some stuff if happening in the background
    private void showLoading(boolean isHidden) {
        binding.loadingBar.setVisibility(isHidden ? View.VISIBLE : View.INVISIBLE);
    }
}
