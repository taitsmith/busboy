package com.taitsmith.busboy.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class NearbyFragment extends Fragment {

    OnItemClickListener listItemListener;
    OnItemLongClickListener listItemLongListener;
    NearbyViewModel nearbyViewModel;
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

        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
}