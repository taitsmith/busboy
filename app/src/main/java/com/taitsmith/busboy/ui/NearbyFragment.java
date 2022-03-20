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
import android.widget.ListView;

import com.taitsmith.busboy.databinding.NearbyFragmentBinding;
import com.taitsmith.busboy.obj.Stop;
import com.taitsmith.busboy.utils.NearbyAdapter;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.viewmodels.NearbyViewModel;
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination;

import static com.taitsmith.busboy.viewmodels.NearbyViewModel.loc;
import static com.taitsmith.busboy.viewmodels.NearbyViewModel.mutableSimpleLocation;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {
    static NearbyViewModel nearbyViewModel;

    OnItemClickListener listItemListener;
    OnItemLongClickListener listItemLongListener;
    ListView nearbyStopListView;
    NearbyFragmentBinding binding;
    List<Stop> nearbyStopList;
    List<List<RouteDestination>> destinationList;
    NearbyAdapter nearbyAdapter;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

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
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nearbyViewModel = new ViewModelProvider(requireActivity()).get(NearbyViewModel.class);
        nearbyViewModel.checkLocationPerm();
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
        mutableSimpleLocation.removeObservers(getViewLifecycleOwner());
        nearbyViewModel.mutableNearbyStops.removeObservers(getViewLifecycleOwner());
    }

    private void setObservers() {
        nearbyViewModel.mutableNearbyStops.observe(getViewLifecycleOwner(), stops -> {
            destinationList = new ArrayList<>();
            this.nearbyStopList = stops;
            for (Stop stop : nearbyStopList) {
                nearbyViewModel.getStopDestinations(stop.getStopId());
            }
//            NearbyViewModel.mutableHashMap.setValue(destinationHashMap);
        });

        mutableSimpleLocation.observe(getViewLifecycleOwner(), simpleLocation ->{
            NearbyViewModel.loc = simpleLocation;
            nearbyViewModel.getNearbyStops();
            loc.endUpdates();
        });

        NearbyViewModel.mutableHashMap.observe(getViewLifecycleOwner(), stringListHashMap -> {
            nearbyAdapter = new NearbyAdapter(stringListHashMap, nearbyStopList);
            nearbyStopListView.setAdapter(nearbyAdapter);

        });
    }
}