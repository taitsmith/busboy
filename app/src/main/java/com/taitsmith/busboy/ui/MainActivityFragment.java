package com.taitsmith.busboy.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;
import com.taitsmith.busboy.utils.NearbyAdapter;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import java.util.List;

public class MainActivityFragment extends Fragment{

    MainActivityViewModel mainActivityViewModel;
    FragmentMainActivityBinding binding;
    EditText stopIdEditText;
    ListView listView; //shows both types of lists //TODO is this bad practice?
    PredictionAdapter predictionAdapter;
    NearbyAdapter nearbyAdapter;
    OnListItemSelectedListener listener;
    TabLayout bottomTabLayout;
    List<Stop> nearbyStopList;
    List<Prediction> predictionList;

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
        bottomTabLayout = binding.mainTabLayout;
        stopIdEditText = binding.stopEntryEditText;
        listView = binding.predOrNearbyListView;

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        String s = stopIdEditText.getText().toString();
                        if (s.length() != 5) mainActivityViewModel.errorMessage.setValue("BAD_INPUT");
                        else {
                            showLoading(true);
                            mainActivityViewModel.getStopPredictions(s);
                        }
                        break;
                    case 1:
                        showLoading(true);
                        mainActivityViewModel.checkLocationPerm();
                        break;
                    case 2:
                        mainActivityViewModel.errorMessage.setValue("HELP_REQUESTED");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (bottomTabLayout.getSelectedTabPosition()) {
                    case 0 :
                        if (predictionAdapter != null) showStopPredictionsList();
                        else {
                            String s = stopIdEditText.getText().toString();
                            if (s.length() != 5) mainActivityViewModel.errorMessage.setValue("BAD_INPUT");
                            else {
                                showLoading(true);
                                mainActivityViewModel.getStopPredictions(s);
                            }
                        }
                        break;
                    case 1:
                        if (nearbyAdapter != null) showNearbyStopList();
                        break;
                }
            }
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
        mainActivityViewModel.mutableStopPredictions.observe(getViewLifecycleOwner(), predictionList -> {
            this.predictionList = predictionList;
            showStopPredictionsList();
        });

        mainActivityViewModel.mutableNearbyStops.observe(getViewLifecycleOwner(), stopList -> {
            this.nearbyStopList = stopList;
            showNearbyStopList();
        });

    }

    //let the user know some stuff if happening in the background
    private void showLoading(boolean isHidden) {
        binding.loadingBar.setVisibility(isHidden ? View.VISIBLE : View.INVISIBLE);
    }

    private void showStopPredictionsList() {
        predictionAdapter = new PredictionAdapter(predictionList);
        listView.setAdapter(predictionAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                listener.onPredictionSelected(i));
        showLoading(false);
    }

    private void showNearbyStopList() {
        nearbyAdapter = new NearbyAdapter(nearbyStopList);
        listView.setAdapter(nearbyAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                listener.onNearbyStopSelected(i));
        showLoading(false);
    }
}
