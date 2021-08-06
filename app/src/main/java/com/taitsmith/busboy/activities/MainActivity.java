package com.taitsmith.busboy.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.taitsmith.busboy.R;

import com.taitsmith.busboy.databinding.ActivityMainBinding;
import com.taitsmith.busboy.fragments.MainActivityFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.fragment.getId(), MainActivityFragment.newInstance())
                    .commitNow();
        }
    }
}
