package com.taitsmith.busboy.ui;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.taitsmith.busboy.databinding.ByIdFragmentBinding;
import com.taitsmith.busboy.di.DatabaseRepository;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.ByIdViewModel;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ByIdFragment extends Fragment {

    @Inject
    DatabaseRepository repository;

    ByIdViewModel byIdViewModel;
    ByIdFragmentBinding binding;
    OnItemClickListener listItemListener;
    OnItemLongClickListener longClickListener;
    ListView predictionListView;
    List<Prediction> predictionList;
    EditText stopIdEditText;
    PredictionAdapter predictionAdapter;
    Stop stop = new Stop();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ByIdFragmentBinding.inflate(inflater, container, false);
        
        predictionListView = binding.predictionListView;
        stopIdEditText = binding.stopEntryEditText;
        
        if (getArguments() != null) {
            byIdViewModel.getStopPredictions(getArguments().get("BY_ID").toString());
        }
        
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.searchByIdButton.setOnClickListener(this::search);
        binding.addToFavoritesFab.setOnClickListener(this::addToFavorites);

        predictionListView.setOnItemClickListener((adapterView, view, i, l) ->
                listItemListener.onIdItemSelected(i));

        predictionListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            longClickListener.onIdLongClick(i);
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        byIdViewModel = new ViewModelProvider(requireActivity()).get(ByIdViewModel.class);
        setObserver();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listItemListener = (OnItemClickListener) context;
            longClickListener = (OnItemLongClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context +
                    "Must Implement OnListItemSelectedListener");
        }
    }

    private void setObserver() {
        byIdViewModel.mutableStopPredictions.observe(getViewLifecycleOwner(), predictions -> {
                    this.predictionList = predictions;
                    predictionAdapter = new PredictionAdapter(predictionList);
                    predictionListView.setAdapter(predictionAdapter);
                    binding.busFlagIV.setVisibility(View.INVISIBLE);

                    try {
                        binding.stopEntryEditText.setText(null);
                        binding.stopEntryEditText.setHint(predictions.get(0).getStpnm());
                    } catch (NullPointerException | IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    mutableStatusMessage.setValue("LOADED");
                }
        );
    }

    private void search(View view) {
        if (binding.stopEntryEditText.getText().length() == 5) {
            mutableStatusMessage.setValue("LOADING");
            byIdViewModel.getStopPredictions(stopIdEditText.getText().toString());
            stop.setStopId(stopIdEditText.getText().toString());
        } else {
            MainActivityViewModel.mutableErrorMessage.setValue("BAD_INPUT");
        }
    }

    private void addToFavorites(View view) {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listItemListener = null;
        longClickListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        byIdViewModel.mutableStopPredictions.removeObservers(getViewLifecycleOwner());
    }
}