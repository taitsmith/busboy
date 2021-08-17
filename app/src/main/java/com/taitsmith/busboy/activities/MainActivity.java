package com.taitsmith.busboy.activities;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.taitsmith.busboy.R;

import com.taitsmith.busboy.databinding.ActivityMainBinding;
import com.taitsmith.busboy.fragments.MainActivityFragment;
import com.taitsmith.busboy.viewmodels.MainActivityViewModel;

import im.delight.android.location.SimpleLocation;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public static SimpleLocation location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        location = new SimpleLocation(this);
        MainActivityViewModel viewModel =
                new ViewModelProvider(this).get(MainActivityViewModel.class);

        viewModel.errorMessage.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                getError(s);
                }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.fragment.getId(), MainActivityFragment.newInstance())
                    .commitNow();
        }
    }

    private void getError(String s) {
        switch (s) {
            case "NO_PERMISSION" :
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 519);
                break;
            case "BAD_STOP" :

        }
    }
}
