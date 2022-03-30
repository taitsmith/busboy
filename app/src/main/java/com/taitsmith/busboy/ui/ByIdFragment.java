package com.taitsmith.busboy.ui;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.taitsmith.busboy.databinding.ByIdFragmentBinding;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.utils.PredictionAdapter;
import com.taitsmith.busboy.viewmodels.ByIdViewModel;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse.Prediction;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ByIdFragment extends Fragment {
    ByIdViewModel byIdViewModel;
    ByIdFragmentBinding binding;

    OnItemClickListener listItemListener;
    OnItemLongClickListener longClickListener;
    ListView predictionListView;
    List<Prediction> predictionList;

    EditText stopIdEditText;
    PredictionAdapter predictionAdapter;

    public static ByIdFragment newInstance() {
        return new ByIdFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ByIdFragmentBinding.inflate(inflater, container, false);
        predictionListView = binding.predictionListView;
        stopIdEditText = binding.stopEntryEditText;

        binding.searchByIdButton.setOnClickListener(this::search);

        predictionListView.setOnItemClickListener((adapterView, view, i, l) ->
                listItemListener.onIdItemSelected(i));
        
        predictionListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            longClickListener.onIdLongClick(i);
            return true;
        });

        if (getArguments() != null) {
            byIdViewModel.getStopPredictions(getArguments().get("BY_ID").toString());
        }

        return binding.getRoot();
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
                    mutableStatusMessage.setValue("LOADED");
                }
        );
    }

    private void search(View view) {
        if (binding.stopEntryEditText.getText().length() == 5) {
            mutableStatusMessage.setValue("LOADING");
            byIdViewModel.getStopPredictions(stopIdEditText.getText().toString());
        } else {
            MainActivityViewModel.mutableErrorMessage.setValue("BAD_INPUT");
        }
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