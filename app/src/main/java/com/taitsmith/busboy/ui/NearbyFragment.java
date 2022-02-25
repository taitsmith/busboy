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
import android.widget.ListView;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.databinding.NearbyFragmentBinding;
import com.taitsmith.busboy.utils.OnItemClickListener;
import com.taitsmith.busboy.utils.OnItemLongClickListener;
import com.taitsmith.busboy.viewmodels.NearbyViewModel;

import static com.taitsmith.busboy.viewmodels.NearbyViewModel.mutableSimpleLocation;

public class NearbyFragment extends Fragment {
    static NearbyViewModel nearbyViewModel;

    OnItemClickListener listItemListener;
    OnItemLongClickListener listItemLongListener;
    ListView nearbyStopListView;
    NearbyFragmentBinding binding;

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
        mutableSimpleLocation.observe(getViewLifecycleOwner(), simpleLocation ->{
            nearbyViewModel.loc = simpleLocation;
            nearbyViewModel.getNearbyStops();
        });
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
    }
}