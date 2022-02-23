package com.taitsmith.busboy.ui;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taitsmith.busboy.R;
import com.taitsmith.busboy.viewmodels.ByIdViewModel;

public class ByIdFragment extends Fragment {

    private ByIdViewModel mViewModel;

    public static ByIdFragment newInstance() {
        return new ByIdFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.by_id_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ByIdViewModel.class);
        // TODO: Use the ViewModel
    }

}