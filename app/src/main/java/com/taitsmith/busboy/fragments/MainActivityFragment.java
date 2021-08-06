package com.taitsmith.busboy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.taitsmith.busboy.databinding.FragmentMainActivityBinding;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

public class MainActivityFragment extends Fragment {

    private MainActivityViewModel mainActivityViewModel;
    private FragmentMainActivityBinding binding;
    Button stopIdButton;
    EditText stopIdEditText;

    public static MainActivityFragment newInstance() {
        return new MainActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainActivityBinding.inflate(inflater, container, false);
        stopIdButton = binding.searchIdButton;
        stopIdEditText = binding.stopEntryEditText;
        stopIdButton.setOnClickListener(view -> {
            mainActivityViewModel.stopId = stopIdEditText.getText().toString();
            mainActivityViewModel.getMutableLivePrediction();
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    }

    private void hideUI(boolean isHidden) {

    }

}
