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
    OnListItemSelectedListener listItemListener;
    OnListItemLongListener listItemLongListener;
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

    public interface OnListItemLongListener {
        void onNearbyLongSelected(int position);
        void onPredictionLongSelected(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listItemListener = (OnListItemSelectedListener) context;
            listItemLongListener = (OnListItemLongListener) context;
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

                switch (tab.getText().toString()) {
                    case "By ID":
                        String s = stopIdEditText.getText().toString();
                        if (s.length() != 5) mainActivityViewModel.mutableStatusMessage.setValue("BAD_INPUT");
                        else {
                            showLoading(true);
                            mainActivityViewModel.getStopPredictions(s);
                        }
                        break;
                    case "Nearby":
                        showLoading(true);
                        mainActivityViewModel.checkLocationPerm();
                        break;
                    case "Favorites":
                        mainActivityViewModel.mutableStatusMessage.setValue("FAVORITES");
                        break;
                    case "Help":
                        mainActivityViewModel.mutableStatusMessage.setValue("HELP_REQUESTED");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getText().toString()) {
                    case "By ID" :
                        if (predictionAdapter != null) showStopPredictionsList();
                        else {
                            String s = stopIdEditText.getText().toString(); //
                            if (s.length() != 5) mainActivityViewModel.mutableStatusMessage.setValue("BAD_INPUT");
                            else {
                                showLoading(true);
                                mainActivityViewModel.getStopPredictions(s);
                            }
                        }
                        break;
                    case "Nearby":
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
            binding.mainFragmentStopName.setText(predictionList.get(0).getStpnm());
            showStopPredictionsList();
        });

        mainActivityViewModel.mutableNearbyStops.observe(getViewLifecycleOwner(), stopList -> {
            this.nearbyStopList = stopList;
            showNearbyStopList();
        });

    }

    //let the user know some stuff if happening in the background
    public void showLoading(boolean isHidden) {
        if (binding.busFlagIV.getVisibility() == View.VISIBLE) {
            binding.busFlagIV.setVisibility(View.INVISIBLE);
        }
        binding.loadingBar.setVisibility(isHidden ? View.VISIBLE : View.INVISIBLE);
    }

    private void showStopPredictionsList() {
        predictionAdapter = new PredictionAdapter(predictionList);
        listView.setAdapter(predictionAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                listItemListener.onPredictionSelected(i));
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            listItemLongListener.onPredictionLongSelected(i);
            return true;
        });
        showLoading(false);
    }

    private void showNearbyStopList() {
        nearbyAdapter = new NearbyAdapter(nearbyStopList);
        listView.setAdapter(nearbyAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
                listItemListener.onNearbyStopSelected(i));
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            listItemLongListener.onNearbyLongSelected(i);
            return true;
        });
                showLoading(false);
    }
}
