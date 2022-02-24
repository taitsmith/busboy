package com.taitsmith.busboy.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

public class MainActivityFragment extends Fragment{

    MainActivityViewModel mainActivityViewModel;
    FragmentMainActivityBinding binding;

    public MainActivityFragment newInstance() {
        return new MainActivityFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainActivityBinding.inflate(inflater, container, false);
        Log.d("MAIN ACTIVITY FRAGMENT ", "ON CREATE VIEW");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
    }

    //let the user know some stuff if happening in the background
    public void showLoading(boolean isHidden) {
        if (binding.busFlagIV.getVisibility() == View.VISIBLE) {
            binding.busFlagIV.setVisibility(View.INVISIBLE);
        }
        binding.loadingBar.setVisibility(isHidden ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
