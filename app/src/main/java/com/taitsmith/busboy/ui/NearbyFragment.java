package com.taitsmith.busboy.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.NearbyFragmentBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.utils.NearbyAdapter;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.viewmodels.NearbyViewModel;

import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableErrorMessage;
import static com.taitsmith.busboy.viewmodels.MainActivityViewModel.mutableStatusMessage;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NearbyFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    static NearbyViewModel nearbyViewModel;

    OnItemClickListener listItemListener;
    OnItemLongClickListener listItemLongListener;
    ListView nearbyStopListView;
    NearbyFragmentBinding binding;
    List<Stop> nearbyStopList;
    List<String> stopNameList;
    NearbyAdapter nearbyAdapter;
    Spinner buslineSpinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = NearbyFragmentBinding.inflate(inflater, container, false);
        nearbyStopListView = binding.nearbyListView;

        nearbyStopListView.setOnItemClickListener((adapterView, view, i, l) ->
                listItemListener.onNearbyItemSelected(i));

        nearbyStopListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            listItemLongListener.onNearbyLongClick(i);
            return true;
        });

        buslineSpinner = binding.buslineSpinner;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.bus_lines, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buslineSpinner.setAdapter(adapter);
        buslineSpinner.setOnItemSelectedListener(this);

        binding.nearbySearchButton.setOnClickListener(view -> {
            String s = binding.nearbyEditText.getText().toString();
            if (s.length() > 0) {
                int distance = Integer.parseInt(s);
                if (distance < 500 || distance > 5000) {
                    mutableErrorMessage.setValue("BAD_DISTANCE");
                } else {
                    nearbyViewModel.setDistance(distance);
                }
            }
            nearbyViewModel.getNearbyStops();
        });
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nearbyViewModel = new ViewModelProvider(requireActivity()).get(NearbyViewModel.class);
        nearbyViewModel.checkLocationPerm();

        if (!MainActivity.enableNearbySearch) {
            binding.nearbySearchButton.setEnabled(false);
        }
        setObservers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listItemListener = (OnItemClickListener) context;
            listItemLongListener = (OnItemLongClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context +
                    "Must Implement OnListItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listItemListener = null;
        listItemLongListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        nearbyViewModel.mutableNearbyStops.removeObservers(getViewLifecycleOwner());
    }

    private void setObservers() {
        nearbyViewModel.mutableNearbyStops.observe(getViewLifecycleOwner(), stops -> {
            if (nearbyAdapter == null) nearbyAdapter = new NearbyAdapter(stops);
            nearbyStopListView.setAdapter(nearbyAdapter);
            mutableStatusMessage.setValue("LOADED");
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String s = adapterView.getItemAtPosition(i).toString();
        if (s.equals("All lines")) nearbyViewModel.setRt(null);
        else nearbyViewModel.setRt(s);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}