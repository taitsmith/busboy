package com.taitsmith.busboy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.taitsmith.busboy.R;

import com.taitsmith.busboy.viewmodels.BusDetailViewModel;

public class BusDetailFragment extends Fragment {

    private BusDetailViewModel mViewModel;

    public static BusDetailFragment newInstance() {
        return new BusDetailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(BusDetailViewModel.class);
        // TODO: Use the ViewModel
    }

}
